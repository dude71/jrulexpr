package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.core.items.ItemRegistry;

public abstract class AbstractJrxItemExpression extends JrxExpression {
    private final JrxItem item;

    AbstractJrxItemExpression(JrxItem item, String xpr) {
        super(xpr);
        this.item = item;
    }

    AbstractJrxItemExpression(JrxItem item, String xpr, ItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(xpr, itemRegistry, functionRegistry);
        this.item = item;   
    }
    
    protected JrxItem getItem() {
        return this.item;
    }

    @Override
    public JrxFunction<? extends Object> prepareFunctionInstance(JrxFunction<? extends Object> f) {
        f.setItem(item);
        f.setItemRegistry(itemRegistry);
        return super.prepareFunctionInstance(f);
    }

    public Boolean evaluateToBoolean() {
        Object eval = evaluate();
        if (eval instanceof Boolean) return (Boolean)eval;
        throw new IllegalStateException(item.getName() + "." + getXpr() + " does not evaluate to Boolean!");
    }
}
