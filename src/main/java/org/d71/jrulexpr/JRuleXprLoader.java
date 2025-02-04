package org.d71.jrulexpr;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.value.JRuleDecimalValue;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class JRuleXprLoader extends JRule {
    private static boolean loaded = false;
    private static final String NR_JRX_LOADED = "NR_JRX_LOADED";

    protected static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprLoader.class);
    protected static int startupWaitMs = 1000;

    static {
        LOGGER.info("JRuleXpr loading..");
        load();
    }

    private static boolean startupWait() {
        try {
            LOGGER.info("JRuleXpr.startupWait " + startupWaitMs + " ms..");
            Thread.sleep(startupWaitMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private synchronized static void load() {
        LOGGER.info("JRuleXpr.load loaded=" + loaded);
        if (!loaded && (!rulesExist() && startupWait() || forceRulesReload())) {
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
            timer.schedule(task, 9000);
        }
    }

    private static boolean rulesExist() {
        boolean rv = false;
        try {
            // TODO read generated classes from items
            Class.forName("org.openhab.automation.jrule.rules.user.generated.NetRules", false, JRuleXprLoader.class.getClassLoader());
            rv = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("JRuleXpr.rulesCreated=" + rv);
        return rv;
    }

    private static boolean forceRulesReload() {
        boolean rv = false;
        try {
            Class<?> loaderClazz = Class.forName("org.openhab.automation.jrule.rules.user.generated.JRuleXprLoader", false, JRuleXprLoader.class.getClassLoader());
            rv = loaderClazz.getResource("jrulexpr-reload") != null || loaderClazz.getResource("generated/jrulexpr-reload") != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("JRuleXpr.forceRulesReload=" + rv);
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
}
