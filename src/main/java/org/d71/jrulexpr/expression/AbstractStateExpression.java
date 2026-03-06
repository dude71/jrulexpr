package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.expression.eval.JRuleValueHandler;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.value.JRuleValue;

public abstract class AbstractStateExpression extends AbstractJRuleXprExpression<JRuleValue> {
    public AbstractStateExpression(JrxItem item) {
        super(item);
    }

    @Override
    protected JRuleValue convertEvaluatedValue(Object valueObj) {
        if (valueObj instanceof JRuleValue) {
            return (JRuleValue) valueObj;
        } else  {
            return JRuleValueHandler.convertObjectToJRuleValue(valueObj, getContainerItem().getType());
        }
    }
}
