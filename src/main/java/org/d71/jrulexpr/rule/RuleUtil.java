package org.d71.jrulexpr.rule;

import org.apache.commons.text.CaseUtils;
import org.openhab.core.items.Item;

public class RuleUtil {
    public static String getMethodName(Item item) {
        return getMethodName(item.getName());
    }
    
    public static String getMethodName(String itemName) {
        return CaseUtils.toCamelCase(itemName, false, '_', '-', ' ');
    } 
}
