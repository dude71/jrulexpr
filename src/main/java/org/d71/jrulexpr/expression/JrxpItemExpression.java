package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Set<String> allowNulls = item.getJrxcValues("allowNulls");
        
        Set<JrxItem> nullItems = item.getTriggeringItems().stream().filter(
            i -> i.getState() == null && !i.getName().equals(item.getName()) && !allowNulls.contains(i.getName())
        ).collect(Collectors.toSet());

        nullItems.stream().forEach(i -> LOGGER.info(">> {} is NULL!", new Object[]{ i.getName() }));
        return nullItems.isEmpty() ? super.evaluateToBoolean() : false;
    }

    @Override
    protected Object getDefaultValue() {
        return Boolean.TRUE;
    }
}
