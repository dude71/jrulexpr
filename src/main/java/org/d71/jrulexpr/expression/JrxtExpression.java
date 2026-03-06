package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.expression.eval.JRuleValueHandler;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.library.CoreItemFactory;

import java.math.BigDecimal;

public class JrxtExpression extends AbstractStateExpression {
    JrxtExpression(JrxItem item) {
        super(item);
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRXT;
    }

    @Override
    protected JRuleValue defaultValue() {
        Object obj;
        String itemType = getContainerItem().getType();
        obj = switch (itemType) {
            case CoreItemFactory.NUMBER -> BigDecimal.ONE;
            case CoreItemFactory.DIMMER -> BigDecimal.valueOf(100);
            case CoreItemFactory.ROLLERSHUTTER -> BigDecimal.valueOf(0);
            case CoreItemFactory.SWITCH -> "ON";
            default -> null;
        };
        return JRuleValueHandler.convertObjectToJRuleValue(obj, itemType);
    }
}
