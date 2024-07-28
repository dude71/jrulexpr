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
        if (!loaded && !rulesExist()) {
            JRuleXpr.getInstance().generateItemRules();
            loaded = true;
            JRuleXpr.getInstance().unload();
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
        LOGGER.info("JRuleXpr.rulesExist=" + rv);
        return rv;
    }
}
