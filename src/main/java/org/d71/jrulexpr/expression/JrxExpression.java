package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;

public class JrxExpression extends AbstractBooleanExpression {
    JrxExpression(JrxItem item) {
        super(item);
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRX;
    }

    @Override
    protected Boolean defaultValue() {
        return Boolean.TRUE;
    }
}
