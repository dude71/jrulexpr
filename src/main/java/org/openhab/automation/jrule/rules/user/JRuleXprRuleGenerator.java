package org.openhab.automation.jrule.rules.user;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.ItemRuleGenerator;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.value.JRuleDecimalValue;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JRuleXprRuleGenerator is responsible for generating JRuleXpr rules on
 * OpenHab startup. It generates item rules based on the JrxItemRegistry.
 * 
 * JRuleXprRuleGenerator is a JRule so that the JRule addon will load it automatically.
 * The JRule addon will recreate all rule classes on any change in (or creation
 * of) a JRule java
 * file in $OH_HOME/automation/jrule/rules. This can cause the RuleGenerator the be
 * executed multiple times in quick succession, which is why the lock mechanism
 * is in place to prevent multiple loads. The RuleGenerator will wait for a configured
 * time before starting rule generation to allow the system to stabilize after
 * startup.
 *
 * The RuleGenerator can be configured with environment variables:
 * - JRULEXPR_STARTUP_WAIT: Time in milliseconds to wait before starting rule
 * generation (default: 5000)
 * - JRULEXPR_RULE_WAIT: Time in milliseconds to wait per generated rule
 * (default: 50)
 * - JRULEXPR_LOADED_WAIT: Time in milliseconds to wait after generating rules
 * before setting loaded state (default: 2000)
 */
public class JRuleXprRuleGenerator extends JRule {

    private static final String GENERATOR_LOCK = "jrx-loader-lock";

    private static final long GENERATOR_LOCK_MS = 25000L;

    private static final String NR_JRX_LOADED = "NR_JRX_LOADED";

    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprRuleGenerator.class);

    static {
        LOGGER.info("## JRuleXprRuleGenerator static initializer called, checking for generation lock.");
        String lock = System.getProperty(GENERATOR_LOCK);
        Long lockEpoch = lock == null ? null : Long.parseLong(lock);
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        if (lockEpoch == null || now > lockEpoch) {
            System.setProperty(GENERATOR_LOCK, Long.valueOf(now + GENERATOR_LOCK_MS).toString());
            generate();
        } else {
            LOGGER.info("JRuleXprRuleGenerator generation already in progress by another instance, skipping generation.");
        }
    }

    private synchronized static void generate() {
        int startupWaitMs = Integer
                .parseInt(Optional.ofNullable(System.getenv("JRULEXPR_STARTUP_WAIT")).orElse("5000"));
        int waitPerRuleMs = Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_RULE_WAIT")).orElse("50"));
        LOGGER.info(">> JRuleXprRuleGenerator.generate: startupWaitMs=" + startupWaitMs + ", waitPerRuleMs=" + waitPerRuleMs);        
        
        Item jrxLoadedItem = getJrxLoadedItem();
        boolean jrxLoaded = jrxLoadedItem != null && DecimalType.valueOf("1").equals(jrxLoadedItem.getStateAs(DecimalType.class));
        LOGGER.debug("JRuleXprRuleGenerator: jrxLoaded=" + jrxLoaded + " (item state: " + (jrxLoadedItem != null ? jrxLoadedItem.getState() : "null") + ")");

        if (!jrxLoaded && startupWaitMs > 0) {
            storeJrxLoaded(0);
            doStartupWaitTimer(startupWaitMs, waitPerRuleMs);
        } else {
            generateItemRules(waitPerRuleMs);
        }
    }

    private static void storeJrxLoaded(int state) {
        LOGGER.info("JRuleXpr setting " + NR_JRX_LOADED + " to " + state);
        Item stateItm = getOrCreateJrxLoadedItem();
        JRuleEventHandler.get().postUpdate(stateItm.getName(), new JRuleDecimalValue(BigDecimal.valueOf(state)));
    }

    private static Item getOrCreateJrxLoadedItem() {
        Item itm = getJrxLoadedItem();
        if (itm == null) {
            LOGGER.info("JRuleXpr creating " + NR_JRX_LOADED);
            itm = JRuleItemHandler.get().addNumberItem(NR_JRX_LOADED, null, "JRuleXpr loaded");
        }
        return itm;
    }

    private static Item getJrxLoadedItem() {
        Item itm;
        try {
            itm = JRuleEventHandler.get().getItemRegistry().getItem(NR_JRX_LOADED);
        } catch (ItemNotFoundException e) {
            itm = null;
        }
        return itm;
    }

    private static void doStartupWaitTimer(int startupWaitMs, int waitPerRuleMs) {
        LOGGER.info("JRuleXprRuleGenerator scheduling startup wait timer for " + startupWaitMs + " ms.");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                generateItemRules(waitPerRuleMs);
            }
        };
        Timer timer = new Timer("jrxStartupWaitTimer");
        timer.schedule(task, startupWaitMs);
    }

    private static void generateItemRules(int waitPerRuleMs) {
        int loadedWaitMs = Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_LOADED_WAIT")).orElse("2000"));
        LOGGER.info("JRuleXprRuleGenerator generating item rules with waitPerRuleMs=" + waitPerRuleMs + " and genWaitMs="
                + loadedWaitMs);
        int generated = 0;
        ItemRuleGenerator generator = new ItemRuleGenerator();

        for (JrxItem item : JrxItemRegistry.getInstance().getRuleItems()) {
            if (!itemRuleClassExist(item)) {
                generator.generate(item);
                generated++;
            } else {
                LOGGER.debug("Rule class already exists for " + item.getName() + ", skipping generation.");
            }
        }
        int wait = loadedWaitMs + (generated * waitPerRuleMs);
        if (generated > 0) {
            generator.makeAll();
            LOGGER.info("JRuleXprRuleGenerator: Generated " + generated + " rules");
        } else {
            LOGGER.info("JRuleXprRuleGenerator: No new rules generated");
        }
        doJrxLoadedUpdateTimer(wait);
    }

    private static boolean itemRuleClassExist(JrxItem item) {
        boolean found;
        try {
            LOGGER.trace("JRuleXprRuleGenerator checking for existing class " + item.getRuleClassName());
            Class.forName(ItemRuleGenerator.RULE_PKG + "." + item.getRuleClassName(), false,
                    JRuleXprRuleGenerator.class.getClassLoader());
            found = true;
        } catch (ClassNotFoundException e) {
            found = false;
        }
        return found;
    }

    private static void doJrxLoadedUpdateTimer(int waitMs) {
        Timer timer = new Timer("jrxStateTimer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                storeJrxLoaded(1);
            }
        };
        LOGGER.info("JRuleXprRuleGenerator scheduling JRX_LOADED update in " + waitMs + " ms.");
        timer.schedule(task, waitMs);
    }
}
