package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.core.items.ItemRegistry;

public class JrxpItemExpression extends AbstractJrxItemExpression {

    public JrxpItemExpression(JrxItem item) {
        super(item, item.getJrxp());
    }

    public JrxpItemExpression(JrxItem item, ItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrxp(), itemRegistry, functionRegistry);
    }
    
    @Override
    protected void validateXpr(String xpr) {
    }

    @Override
    protected Object getDefaultValue() {
        return Boolean.TRUE;
    }    
}
