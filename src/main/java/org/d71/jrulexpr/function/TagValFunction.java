package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;

@FunctionParameter(name = "tagName")
public class TagValFunction extends AbstractFunction implements JrxFunction<String> {
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
       return parameters.length > 0 ? item.getTagValue((String) parameters[0]).orElse(null) : null;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        return EvaluationValue.stringValue(getValue(parameters[0].getStringValue()));
    }

}
