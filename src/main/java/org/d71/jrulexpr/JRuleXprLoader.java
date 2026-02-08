package org.d71.jrulexpr;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.ItemRuleGenerator;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.value.JRuleDecimalValue;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class JRuleXprLoader extends JRule {

    private static final String LOADER_LOCK = "jrx-loader-lock";

    private static final long LOADER_LOCK_MS = 20000L;

    private static final String NR_JRX_LOADED = "NR_JRX_LOADED";

    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprLoader.class);

    static {
        String lock = System.getProperty(LOADER_LOCK);
        Long lockEpoch = lock == null ? null : Long.parseLong(lock);
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        if (lockEpoch == null || now > lockEpoch) {
            System.setProperty(LOADER_LOCK, Long.valueOf(now + LOADER_LOCK_MS).toString());
            load();
        } else {
            LOGGER.info("JRuleXprLoader: another instance is loading or recently loaded, skipping load. lockEpoch="
                    + lockEpoch + ", now=" + now);
        }
    }

    private synchronized static void load() {
        int startupWaitMs = Integer
                .parseInt(Optional.ofNullable(System.getenv("JRULEXPR_STARTUP_WAIT")).orElse("5000"));
        int waitPerRuleMs = Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_RULE_WAIT")).orElse("50"));
        LOGGER.info("## JRuleXprLoader.load: startupWaitMs=" + startupWaitMs + ", waitPerRuleMs=" + waitPerRuleMs);
        storeJrxLoaded(0);
        if (startupWaitMs > 0) {
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
        LOGGER.info("JRuleXprLoader scheduling startup wait timer for " + startupWaitMs + " ms.");
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
        LOGGER.info("JRuleXprLoader generating item rules with waitPerRuleMs=" + waitPerRuleMs + " and genWaitMs="
                + loadedWaitMs);
        int generated = 0;
        ItemRuleGenerator generator = new ItemRuleGenerator();

        for (JrxItem item : JrxItemRegistry.getInstance().getRuleItems()) {
            if (!itemRuleClassExist(item)) {
                generator.generate(item);
                generated++;
            } else {
                LOGGER.info("Rule class already exists for " + item.getName() + ", skipping generation.");
            }
        }
        int wait = loadedWaitMs + (generated * waitPerRuleMs);
        if (generated > 0) {
            generator.makeAll();
            LOGGER.info("JRuleXprLoader: Generated " + generated + " rules");
        } else {
            LOGGER.info("JRuleXprLoader: No new rules generated");
        }
        doJrxLoadedUpdateTimer(wait);
    }

    private static boolean itemRuleClassExist(JrxItem item) {
        boolean found;
        try {
            LOGGER.debug("JRuleXprLoader checking for existing class " + item.getRuleClassName());
            Class.forName(ItemRuleGenerator.RULE_PKG + "." + item.getRuleClassName(), false,
                    JRuleXprLoader.class.getClassLoader());
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
        LOGGER.info("JRuleXprLoader scheduling JRX_LOADED update in " + waitMs + " ms.");
        timer.schedule(task, waitMs);
    }
}
