package org.d71.jrulexpr.expression;

import java.util.Map;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJrxItemExpression extends JrxExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJrxItemExpression.class);
    private final JrxItem item;

    private static String expandXpr(JrxItem item, String xpr) {
        LOGGER.debug("expandXpr: item: {} xpr: {}", new Object[] { item.getName(), xpr });
        String expanded = xpr;
        if (xpr != null) {
            for (Map.Entry<String, String> e : item.getJrxVars().entrySet()) {
                // TODO fix when var matches start of another var
                expanded = expanded.replaceAll(e.getKey(), "(" + e.getValue() + ")");
            }
        }
        return expanded;
    }

    AbstractJrxItemExpression(JrxItem item, String xpr) {
        super(expandXpr(item, xpr));
        this.item = item;
    }

    AbstractJrxItemExpression(JrxItem item, String xpr, JrxItemRegistry itemRegistry,
            JrxFunctionRegistry functionRegistry) {
        super(expandXpr(item, xpr), itemRegistry, functionRegistry);
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
        if (eval instanceof Boolean)
            return (Boolean) eval;
        throw new IllegalStateException(item.getName() + "." + getXpr() + " does not evaluate to Boolean!");
    }
}
