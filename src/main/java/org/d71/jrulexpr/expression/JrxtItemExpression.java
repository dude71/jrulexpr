package org.d71.jrulexpr.expression;

import java.math.BigDecimal;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;

public class JrxtItemExpression extends AbstractJrxItemExpression {

    public JrxtItemExpression(JrxItem item) {
        super(item, item.getJrxt());
    }

    public JrxtItemExpression(JrxItem item, ItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrxt(), itemRegistry, functionRegistry);
    }
    
    @Override
    protected void validateXpr(String xpr) {
    }

    @Override
    protected Object getDefaultValue() {
        Object rv = null;
        JrxItem item = getItem();
        if (CoreItemFactory.DIMMER.equals(item.getType())) {
            rv = BigDecimal.valueOf(90);
        } else if (CoreItemFactory.NUMBER.equals(item.getType())) {
            rv = BigDecimal.valueOf(1);
        } else if (CoreItemFactory.SWITCH.equals(item.getType())) {
            rv = "ON";
        }
        return rv;
    }    
}