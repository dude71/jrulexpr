package org.d71.jrulexpr.rule;

import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class RuleUtils {
    public static String eventInfo(JRuleEvent event) {
        String rv;
        if (event instanceof JRuleItemEvent itemEvent) {
            rv = itemEvent.getItem().toString() + ", " + itemEvent.getOldState() + "->" + itemEvent.getState();
        } else {
            rv = event.getClass().getSimpleName();
        }
        return rv;
    }
}
