package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionParameter(name = "tagName")
@FunctionParameter(name = "itemName", isVarArg = true)
public class TagVal extends AbstractFunction implements JrxFunction<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagVal.class);
    protected JrxItemRegistry itemRegistry;

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    private JrxItem item;

    @Override
    public String getToken() {
        return "TAGVAL";
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public String getValue(Object... parameters) {
        JrxItem itm = parameters.length == 2 ? itemRegistry.getItem((String)parameters[1]) : item;
        String val = parameters.length > 0 ? itm.getTagValue((String) parameters[0]).orElse(null) : null;
        LOGGER.trace("value: " + val);
        return val;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        String value = parameters.length == 2 ? getValue(parameters[0].getStringValue(), parameters[1].getStringValue()) : getValue(parameters[0].getStringValue());
        return value == null ? EvaluationValue.NULL_VALUE : EvaluationValue.stringValue(value);
    }

}
