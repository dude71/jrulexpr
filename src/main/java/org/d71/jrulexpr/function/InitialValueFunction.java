package org.d71.jrulexpr.function;

import java.math.BigDecimal;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.openhab.core.items.ItemRegistry;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public class InitialValueFunction extends AbstractFunction implements JrxFunction<Object> {
    private JrxItem item;

    private JrxItemRegistry itemRegistry;

    @Override
    public EvaluationValue evaluate(Expression arg0, Token arg1, EvaluationValue... parameters) throws EvaluationException {
        Object itemName = parameters.length == 0 ? item.getName() : parameters[0];
        Object val = getValue(itemName);
        EvaluationValue eval = null;
        if (val instanceof BigDecimal) eval = EvaluationValue.numberValue((BigDecimal)val);
        else if (val instanceof String) eval = EvaluationValue.stringValue((String)val);
        // TODO extend
        return eval;
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    @Override
    public String getToken() {
        return "INITVAL";
    }

    @Override
    public Object getValue(Object... parameters) {
        String itemName = (String)parameters[0];
        //JrxItem item = itemName == this.item.getName() ? this.item : itemRegistry.getItem(itemName);
        
        return null;
    }
    
}
