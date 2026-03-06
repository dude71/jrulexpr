package org.d71.jrulexpr.expression;

import java.math.BigDecimal;

import org.d71.jrulexpr.expression.eval.JRuleValueHandler;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.library.CoreItemFactory;

public class JrxfExpression extends AbstractStateExpression {
    JrxfExpression(JrxItem item) {
        super(item);
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRXF;
    }

    @Override
    protected JRuleValue defaultValue() {
        Object obj;
        String itemType = getContainerItem().getType();
        obj = switch (itemType) {
            case CoreItemFactory.NUMBER, CoreItemFactory.DIMMER -> BigDecimal.ZERO;
            case CoreItemFactory.ROLLERSHUTTER -> BigDecimal.valueOf(100);
            case CoreItemFactory.SWITCH -> "OFF";
            default -> null;
        };
        return JRuleValueHandler.convertObjectToJRuleValue(obj, itemType);    
    }
}
