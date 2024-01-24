package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.core.items.ItemRegistry;

public class JrxItemExpression extends AbstractJrxItemExpression {
    public JrxItemExpression(JrxItem item) {
        super(item, item.getJrx());
    }

    public JrxItemExpression(JrxItem item, ItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrx(), itemRegistry, functionRegistry);
    }

    @Override
    protected void validateXpr(String xpr) {
        super.validateXpr(xpr);
        if (xpr == null || xpr.isBlank()) {
            throw new IllegalStateException("jrx tag must be present!");
        }
    }     
}
