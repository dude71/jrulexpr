package org.d71.jrulexpr.item;

import java.math.BigDecimal;
import java.util.Date;

import org.openhab.automation.jrule.rules.value.*;
import org.openhab.core.library.CoreItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * String value (xpr) -> Object value (evalex) <-> JRuleValue (jrule)
 * 
 */
public class ValueConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);

    public static JRuleValue convertStringToValue(String value, String itemType) {
        return convertObjectToValue(convertStringToObject(value, itemType), itemType);
    }

    public static Object convertStringToObject(String value, String itemType) { 
        Object object = null;
        if (!"NULL".equals(object)) {
            if (CoreItemFactory.DIMMER.equals(itemType) || CoreItemFactory.ROLLERSHUTTER.equals(itemType)) { 
                object = BigDecimal.valueOf(Integer.parseInt(value)); 
            } else if (CoreItemFactory.NUMBER.equals(itemType)) { 
                object = BigDecimal.valueOf(Double.parseDouble(value)); 
            } else if (CoreItemFactory.DATETIME.equals(itemType)) { 
                object = BigDecimal.valueOf(Long.parseLong(value));
            } else if (CoreItemFactory.SWITCH.equals(itemType)) { 
                object = (JRuleOnOffValue.valueOf(value)).stringValue(); 
            } else if (CoreItemFactory.COLOR.equals(itemType) || CoreItemFactory.STRING.equals(itemType)) { 
                object = value;
            } else {
                LOGGER.warn("Cannot convert string value={} to object for item type {}!", new Object[]{value, itemType});
            }
        }
        return object; 
    }
        
    public static JRuleValue convertObjectToValue(Object object, String itemType) {
        JRuleValue rv = null;
        if (object != null) {
            if (object instanceof BigDecimal) {
                if (CoreItemFactory.DIMMER.equals(itemType)) {
                    rv = new JRulePercentValue(((BigDecimal) object).intValue());
                } else if (CoreItemFactory.NUMBER.equals(itemType)) {
                    rv = new JRuleDecimalValue((BigDecimal) object);
                } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                    rv = new JRuleDateTimeValue(new Date(((BigDecimal) object).longValue()));
                } else if (CoreItemFactory.ROLLERSHUTTER.equals(itemType)) {
                    rv = new JRulePercentValue(((BigDecimal) object).intValue());
                }
            } else if (object instanceof String) {
                if (CoreItemFactory.SWITCH.equals(itemType)) {
                    rv = JRuleOnOffValue.valueOf((String) object);
                } else if (CoreItemFactory.STRING.equals(itemType)) {
                    rv = new JRuleStringValue((String) object);
                } else if (CoreItemFactory.COLOR.equals(itemType)) {
                    rv = new JRuleHsbValue((String) object);
                }
            } else {
                LOGGER.warn("Cannot convert object type={}, value={} to value!", new Object[]{object, itemType});
            }
        }
        return rv;
    }

    /**
     * Convert JRuleValue to Object value (only returns EvalEx compatible types, e.g. BigDecimal, String)
     * 
     * @param value
     * @return
     */
    public static Object convertValueToObject(JRuleValue value) {
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
            LOGGER.warn("Cannot convert value {} to object!", new Object[]{value});
        return obj;
    }
}
