package org.d71.jrulexpr.expression;

import java.math.BigDecimal;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.openhab.core.library.CoreItemFactory;

public class JrxfItemExpression extends AbstractJrxItemExpression {

    public JrxfItemExpression(JrxItem item) {
        super(item, item.getJrxf());
    }

    public JrxfItemExpression(JrxItem item, JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        super(item, item.getJrxf(), itemRegistry, functionRegistry);
    }
    
    @Override
    protected void validateXpr(String xpr) {
    }

    @Override
    protected Object getDefaultValue() {
        Object rv = null;
        JrxItem item = getItem();
        if (CoreItemFactory.DIMMER.equals(item.getType())) {
            rv = BigDecimal.valueOf(0);
        } else if (CoreItemFactory.NUMBER.equals(item.getType())) {
            rv = BigDecimal.valueOf(0);
        } else if (CoreItemFactory.SWITCH.equals(item.getType())) {
            rv = "OFF";
        }
        return rv;
    }    
}