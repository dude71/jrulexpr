package org.d71.jrulexpr.expression.eval;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.openhab.automation.jrule.rules.value.JRuleDateTimeValue;
import org.openhab.automation.jrule.rules.value.JRuleDecimalValue;
import org.openhab.automation.jrule.rules.value.JRuleHsbValue;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;
import org.openhab.automation.jrule.rules.value.JRulePercentValue;
import org.openhab.automation.jrule.rules.value.JRulePlayPauseValue;
import org.openhab.automation.jrule.rules.value.JRuleStringValue;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.library.CoreItemFactory;

public class JRuleValueHandler {

    public static Object extractJRuleValue(JRuleValue value) {
        Object valueObject;

        if (value == null)
            valueObject = null;
        else if (value instanceof JRuleDecimalValue)
            valueObject = ((JRuleDecimalValue) value).getValue();
        else if (value instanceof JRuleOnOffValue)
            valueObject = ((JRuleOnOffValue) value).stringValue();
        else if (value instanceof JRulePercentValue)
            valueObject = BigDecimal.valueOf(((JRulePercentValue) value).intValue());
        else if (value instanceof JRuleDateTimeValue)
            // TODO ZonedDateTime or LocalDateTime? -> for now use epoch millis as number
            // value
            valueObject = BigDecimal.valueOf(((JRuleDateTimeValue) value).getValue().toInstant().toEpochMilli());
        else if (value instanceof JRuleStringValue)
            valueObject = value.stringValue();
        else if (value instanceof JRuleHsbValue)
            valueObject = ((JRuleHsbValue) value).stringValue();
        else if (value instanceof JRulePlayPauseValue)
            valueObject = ((JRulePlayPauseValue) value).stringValue();
        else
            throw new IllegalStateException(
                    "Cannot convert JRuleValue of type " + value.getClass().getSimpleName() + " to EvaluationValue!");

        return valueObject;
    }

    public static JRuleValue convertObjectToJRuleValue(Object object, String itemType) {
        JRuleValue rv;
        if (object == null) {
            rv = null;
        } else if (object instanceof BigDecimal bdObj) {
            rv = convertBigDecimalToJRuleValue(bdObj, itemType);
        } else if (object instanceof String strObj) {
            rv = convertStringToJRuleValue(strObj, itemType);
        } else if (object instanceof List<?> listObj) {
            rv = convertListToJRuleValue(listObj, itemType);
        } else {
            throw createIllegalStateException(object, itemType);
        }
        return rv;
    }

    private static JRuleValue convertListToJRuleValue(List<?> listObj, String itemType) {
        JRuleValue rv;
        if (CoreItemFactory.STRING.equals(itemType)) {
            rv = new JRuleStringValue(String.valueOf(listObj.size()));
        } else if (CoreItemFactory.NUMBER.equals(itemType)) {
            rv = new JRuleDecimalValue(new BigDecimal(listObj.size()));
        } else {
            throw createIllegalStateException(listObj, itemType);
        }
        return rv;
    }

    private static JRuleValue convertStringToJRuleValue(String strObj, String itemType) {
        JRuleValue rv;
        if (CoreItemFactory.SWITCH.equals(itemType)) {
            rv = JRuleOnOffValue.valueOf(strObj);
        } else if (CoreItemFactory.STRING.equals(itemType)) {
            rv = new JRuleStringValue(strObj);
        } else if (CoreItemFactory.COLOR.equals(itemType)) {
            rv = new JRuleHsbValue(strObj);
        } else if (CoreItemFactory.NUMBER.equals(itemType)) {
            rv = new JRuleDecimalValue(new BigDecimal(strObj));
        } else if (CoreItemFactory.PLAYER.equals(itemType)) {
            rv = JRulePlayPauseValue.getValueFromString(strObj);
        } else {
            throw createIllegalStateException(strObj, itemType);
        }
        return rv;
    }

    private static JRuleValue convertBigDecimalToJRuleValue(BigDecimal bdObj, String itemType) {
        JRuleValue rv;
        if (CoreItemFactory.DIMMER.equals(itemType)) {
            rv = new JRulePercentValue(bdObj.intValue());
        } else if (CoreItemFactory.NUMBER.equals(itemType)) {
            rv = new JRuleDecimalValue(bdObj);
        } else if (CoreItemFactory.DATETIME.equals(itemType)) {
            rv = new JRuleDateTimeValue(new Date(bdObj.longValueExact()));
        } else if (CoreItemFactory.ROLLERSHUTTER.equals(itemType)) {
            rv = new JRulePercentValue(bdObj.intValue());
        } else if (CoreItemFactory.STRING.equals(itemType))
            rv = new JRuleStringValue(bdObj.toString());
        else {
            throw createIllegalStateException(bdObj, itemType);
        }
        return rv;
    }

    private static IllegalStateException createIllegalStateException(Object object, String itemType) {
        return new IllegalStateException("Cannot convert object type=" + object.getClass().getSimpleName()
                + ", value=" + object + " to JRuleValue for item type " + itemType + "!");
    }
}
