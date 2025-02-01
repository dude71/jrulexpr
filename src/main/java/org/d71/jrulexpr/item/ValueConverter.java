package org.d71.jrulexpr.item;

import java.math.BigDecimal;
import java.util.Date;

import org.openhab.automation.jrule.rules.value.*;
import org.openhab.core.library.CoreItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);

    public static JRuleValue convertStringToValue(String value, String itemType) {
        Object object = null;
        if (CoreItemFactory.DIMMER.equals(itemType)) {
            object = BigDecimal.valueOf(Integer.parseInt(value));
        } else if (CoreItemFactory.NUMBER.equals(itemType)) {
            object = BigDecimal.valueOf(Double.parseDouble(value));
        } else if (CoreItemFactory.SWITCH.equals(itemType)) {
            object = JRuleOnOffValue.valueOf(value);
        } // TODO rest
        return object == null ? null : convertToValue(object, itemType);
    }

    public static JRuleValue convertToValue(Object object, String itemType) {
        JRuleValue rv = null;
        if (CoreItemFactory.DIMMER.equals(itemType)) {
            rv = new JRulePercentValue(((BigDecimal) object).intValue());
        } else if (CoreItemFactory.NUMBER.equals(itemType)) {
            rv = new JRuleDecimalValue((BigDecimal) object);
        } else if (CoreItemFactory.SWITCH.equals(itemType)) {
            rv = String.valueOf(object).equals("ON") ? JRuleOnOffValue.ON : JRuleOnOffValue.OFF;
        } else if (CoreItemFactory.DATETIME.equals(itemType)) {
            rv = new JRuleDateTimeValue(new Date(((BigDecimal) object).longValue()));
        } else if (CoreItemFactory.STRING.equals(itemType)) {
            rv = new JRuleStringValue((String) object);
        } else if (CoreItemFactory.COLOR.equals(itemType)) {
            rv = new JRuleHsbValue((String) object);
        } else {
            LOGGER.warn("Cannot convert object {} to value!", new Object[] { object });
        }
        return rv;
    }

    public static Object convertToObject(JRuleValue value) {
        Object obj = null;
        if (value instanceof JRuleDecimalValue)
            obj = ((JRuleDecimalValue) value).getValue();
        else if (value instanceof JRuleOnOffValue)
            obj = ((JRuleOnOffValue) value).stringValue();
        else if (value instanceof JRulePercentValue)
            obj = BigDecimal.valueOf(((JRulePercentValue) value).intValue());
        else if (value instanceof JRuleDateTimeValue)
            obj = BigDecimal.valueOf(((JRuleDateTimeValue) value).getValue().toInstant().toEpochMilli());
        else if (value instanceof JRuleStringValue)
            obj = value.stringValue();
        else
            LOGGER.warn("Cannot convert value {} to object!", new Object[] { value });
        return obj;
    }
}
