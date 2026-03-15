package org.d71.jrulexpr.expression;

import java.util.Optional;

import org.d71.jrulexpr.item.JrxItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxvExpression extends AbstractJRuleXprExpression<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxvExpression.class);

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

    @Override
    protected Object evaluatedValue() {
        Object value;
        JrxItem item = getContainerItem();
        Optional<Object> optValue = item.getJrxVarCachedValue(name);
        if (optValue == null) { // null == not in cache
            value = super.evaluatedValue();
            item.putJrxVarValueInCache(name, value);
        } else {
            value = optValue.get();
            LOGGER.debug("Cached value {} for {}.{} retrieved from cache.", new Object[] { value, item.getName(), name });
        }
        return value;
    }
}
