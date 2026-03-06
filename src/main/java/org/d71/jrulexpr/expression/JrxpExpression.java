package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;

public class JrxpExpression extends AbstractBooleanExpression {
    JrxpExpression(JrxItem item) {
        super(item);
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRXP;
    }

    @Override
    protected Boolean defaultValue() {
        return Boolean.TRUE;
    }
}
