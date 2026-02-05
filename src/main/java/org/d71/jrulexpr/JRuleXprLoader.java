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

public class JRuleXprLoader extends JRule implements Runnable {
    static {
        try {
            // load in separate thread not block JRule startup (JRule can pickup generated Java
            // files)
            Thread loaderThread = new Thread(new JRuleXprLoader());
            loaderThread.start();
        } catch (Throwable t) {
            LoggerFactory.getLogger(JRuleXprLoader.class).error("Failed to start JRuleXprLoader thread", t);
        }
    }

    private static final String NR_JRX_LOADED = "NR_JRX_LOADED";

    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprLoader.class);

    private static boolean loaded = false;

    private static boolean startupWait(int startupWaitMs) {
        try {
            // wait for Openhab to load all items before generating rules
            LOGGER.info("JRuleXpr.startupWait " + startupWaitMs + " ms..");
            if (startupWaitMs > 0) {
                Thread.sleep(startupWaitMs);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private synchronized static void load(int startupWaitMs, int waitPerRuleMs) {
        LOGGER.info("JRuleXpr.load loaded=" + loaded);
        if (!loaded && !rulesExist() && startupWait(startupWaitMs)) {
            storeJrxLoaded(0);
            JRuleXpr.getInstance().generateItemRules();
            loaded = true;
            JRuleXpr.getInstance().unload();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    storeJrxLoaded(1);
                }
            };
            Timer timer = new Timer("jrxStateTimer");
            int nrOfRules = JrxItemRegistry.getInstance().getRuleItems().size();
            int wait = nrOfRules * waitPerRuleMs + 1000;
            LOGGER.info("JRuleXprLoader scheduling JRX_LOADED update in " + wait + " ms for " + nrOfRules + " rules.");
            timer.schedule(task, wait);
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

    @Override
    public void run() {
        load(Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_STARTUP_WAIT")).orElse("5000")),
                Integer.parseInt(Optional.ofNullable(System.getenv("JRULEXPR_RULE_WAIT")).orElse("50")));
    }
}
