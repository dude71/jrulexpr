package org.d71.jrulexpr.rule;

import org.apache.commons.text.CaseUtils;
import org.openhab.core.items.Item;

public class RuleUtil {
    public static String getMethodName(Item item) {
        return CaseUtils.toCamelCase(item.getName(), false, '_', '-', ' ');
    } 
}
