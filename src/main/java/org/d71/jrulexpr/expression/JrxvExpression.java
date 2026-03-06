package org.d71.jrulexpr.expression;

import java.util.Optional;

import org.d71.jrulexpr.item.JrxItem;

public class JrxvExpression extends AbstractJRuleXprExpression<Object> {
    private final String name;

    JrxvExpression(JrxItem item, String name) {
        super(item);
        this.name = name;
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRXV;
    }

    @Override
    protected Object convertEvaluatedValue(Object valueObj) {
        return valueObj;
    }

    @Override
    public Optional<String> getDefinition() {
        return getContainerItem().getJrxVar(name);
    }

    @Override
    protected Object defaultValue() {
        throw new UnsupportedOperationException("jrxv expressions must have a definition!");
    }
}
