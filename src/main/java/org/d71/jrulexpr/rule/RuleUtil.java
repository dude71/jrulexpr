package org.d71.jrulexpr.rule;

import org.apache.commons.text.CaseUtils;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.core.items.Item;

public class RuleUtil {
    public static String getMethodName(Item item) {
        return CaseUtils.toCamelCase(item.getName(), false, '_', '-', ' ');
    }

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
