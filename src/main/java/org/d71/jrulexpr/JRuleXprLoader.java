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
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class JRuleXprLoader extends JRule {

    private static final String LOADER_LOCK = "jrx-loader-lock";

    private static final String NR_JRX_LOADED = "NR_JRX_LOADED";

    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprLoader.class);

    static {
        if (System.getProperty(LOADER_LOCK) == null) {
            System.setProperty(LOADER_LOCK, "true");
            load();        
        } else {
            LOGGER.info("JRuleXprLoader static initializer: jrx-loader-lock is already set, skipping load.");
        }
    }

    private synchronized static void load() {
        int startupWaitMs = Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_STARTUP_WAIT")).orElse("5000"));
        int waitPerRuleMs = Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_RULE_WAIT")).orElse("50"));
        LOGGER.info("## JRuleXprLoader.load: startupWaitMs=" + startupWaitMs + ", waitPerRuleMs=" + waitPerRuleMs);
        storeJrxLoaded(0);
        if (rulesExist()) {
            LOGGER.info("JRuleXprLoader.load: rules already exist, skipping generation.");
            doJrxLoadedUpdateTimer(startupWaitMs);
        } else {
            if (startupWaitMs > 0) {
                doStartupWaitTimer(startupWaitMs, waitPerRuleMs);
            } else {
                generateItemRules(waitPerRuleMs);
            }
        }
    }

    private static boolean rulesExist() {
        boolean rv = false;
        try {
            Optional<String> ruleClass = JrxItemRegistry.getInstance().getRuleItems().stream().findFirst()
                    .map(JrxItem::getRuleClassName);
            if (!ruleClass.isEmpty()) {
                LOGGER.info("JRuleXprLoader checking for existing class " + ruleClass.get());
                Class.forName(ItemRuleGenerator.RULE_PKG + "." + ruleClass.get(), false,
                        JRuleXprLoader.class.getClassLoader());
                rv = true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getClass().getCanonicalName() + " " + e.getMessage());
        }
        LOGGER.info("JRuleXpr.rulesExist=" + rv);
        return rv;
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
        LOGGER.info("JRuleXprLoader generating item rules with waitPerRuleMs=" + waitPerRuleMs + " and genWaitMs=" + loadedWaitMs);
        JRuleXprRulesGenerator.getInstance().generateItemRules();
        int nrOfRules = JrxItemRegistry.getInstance().getRuleItems().size();
        int wait = loadedWaitMs + (nrOfRules * waitPerRuleMs);
        doJrxLoadedUpdateTimer(wait);
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
