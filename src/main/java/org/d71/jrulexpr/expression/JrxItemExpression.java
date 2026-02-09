package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;

public class JrxItemExpression extends AbstractJrxItemExpression {
    public JrxItemExpression(JrxItem item) {
        super(item, item.getJrx());
    }

    public JrxItemExpression(JrxItem item, JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrx(), itemRegistry, functionRegistry);
    }    
}
