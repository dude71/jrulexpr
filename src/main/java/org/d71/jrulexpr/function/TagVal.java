package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionParameter(name = "tagName")
public class TagVal extends AbstractFunction implements JrxFunction<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagVal.class);

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
        String val = parameters.length > 0 ? item.getTagValue((String) parameters[0]).orElse(null) : null;
        LOGGER.trace("value: " + val);
        return val;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        String value = getValue(parameters[0].getStringValue());
        return value == null ? EvaluationValue.NULL_VALUE : EvaluationValue.stringValue(value);
    }

}
