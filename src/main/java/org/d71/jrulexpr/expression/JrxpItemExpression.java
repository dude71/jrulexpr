package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class JrxpItemExpression extends AbstractJrxItemExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxpItemExpression.class);

    public JrxpItemExpression(JrxItem item) {
        super(item, item.getJrxp());
    }

    public JrxpItemExpression(JrxItem item, JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrxp(), itemRegistry, functionRegistry);
    }

    @Override
    public Boolean evaluateToBoolean() {
        JrxItem item = getItem();
        LOGGER.info("{}: [jrxp] {}, [jrx] {}", new Object[] { item.getName(), this.getXpr(), item.getJrx() });

        Set<JrxItem> nullItems = item.getTriggeringItems().stream().filter(i -> i.getState() == null).collect(Collectors.toSet());
        nullItems.stream().forEach(i -> LOGGER.info(">> {} is NULL!", new Object[]{ i.getName() }));

        return super.evaluateToBoolean();
    }

    @Override
    protected Object getDefaultValue() {
        return Boolean.TRUE;
    }
}
