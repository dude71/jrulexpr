package org.d71.jrulexpr;

import org.openhab.automation.jrule.rules.JRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JRuleXprLoader extends JRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprLoader.class);
    private static boolean loaded = false;

    static {
        LOGGER.info("JRuleXpr loading..");
        load();
    }

    private synchronized static void load() {
        LOGGER.info("JRuleXpr.load loaded=" + loaded);
        if (!loaded || forceRulesReload()) {
            JRuleXpr.getInstance().generateItemRules();
            loaded = true;
            JRuleXpr.getInstance().unload();
        }
    }

    private static boolean forceRulesReload() {
        boolean rv = false;
        try {
            Class loaderClazz = Class.forName("org.openhab.automation.jrule.rules.user.JRuleXprLoader", false, JRuleXprLoader.class.getClassLoader());
            rv = loaderClazz.getResource("/jrulexpr-reload") != null || loaderClazz.getResource("/generated/jrulexpr-reload") != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("JRuleXpr.forceRulesReload=" + rv);
        return rv;
    }
}
