package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;

public abstract class AbstractBooleanExpression extends AbstractJRuleXprExpression<Boolean> {
    AbstractBooleanExpression(JrxItem item) {
        super(item);
    }

    @Override
    protected Boolean convertEvaluatedValue(Object valueObj) {
        if (valueObj instanceof Boolean) {
            return (Boolean) valueObj;
        } else {
            throw valueConversionException(valueObj);        
        }
    }
}
