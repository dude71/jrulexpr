package org.d71.jrulexpr.expression.eval;

import java.math.BigDecimal;
import java.util.Date;

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
        } else if (object instanceof BigDecimal) {
            if (CoreItemFactory.DIMMER.equals(itemType)) {
                rv = new JRulePercentValue(((BigDecimal) object).intValue());
            } else if (CoreItemFactory.NUMBER.equals(itemType)) {
                rv = new JRuleDecimalValue((BigDecimal) object);
            } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                rv = new JRuleDateTimeValue(new Date(((BigDecimal) object).longValueExact()));
            } else if (CoreItemFactory.ROLLERSHUTTER.equals(itemType))
                rv = new JRulePercentValue(((BigDecimal) object).intValue());
            else {
                throw createIllegalStateException(object, itemType);
            }
        } else if (object instanceof String) {
            if (CoreItemFactory.SWITCH.equals(itemType)) {
                rv = JRuleOnOffValue.valueOf((String) object);
            } else if (CoreItemFactory.STRING.equals(itemType)) {
                rv = new JRuleStringValue((String) object);
            } else if (CoreItemFactory.COLOR.equals(itemType)) {
                rv = new JRuleHsbValue((String) object);
            } else if (CoreItemFactory.NUMBER.equals(itemType)) {
                rv = new JRuleDecimalValue(new BigDecimal((String) object));
            } else if (CoreItemFactory.PLAYER.equals(itemType)) {
                rv = JRulePlayPauseValue.getValueFromString((String) object);
            }
            else {
                throw createIllegalStateException(object, itemType);
            }
        } else {
            throw createIllegalStateException(object, itemType);
        }
        return rv;
    }

    private static IllegalStateException createIllegalStateException(Object object, String itemType) {
        return new IllegalStateException("Cannot convert object type=" + object.getClass().getSimpleName()
                + ", value=" + object + " to JRuleValue for item type " + itemType + "!");
    }
}
