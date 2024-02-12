package org.d71.jrulexpr.function;

import java.math.BigDecimal;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "string")
@FunctionParameter(name = "start")
@FunctionParameter(name = "end")
public class SubStrFunction extends AbstractFunction implements JrxFunction<String> {
    @Override
    public String getToken() {
        return "SUBSTR";
    }

    @Override
    public String getValue(Object... parameters) {
        if (parameters.length == 3) {
            return ((String) parameters[0]).substring(((BigDecimal) parameters[1]).intValue(),
                    ((BigDecimal) parameters[2]).intValue());
        } else {
            throw new RuntimeException("SUBSTR needs 3 parameters");
        }
    }

    @Override
    public EvaluationValue evaluate(Expression arg0, Token arg1, EvaluationValue... parameters)
            throws EvaluationException {
        return EvaluationValue.stringValue(getValue(parameters[0].getStringValue(), parameters[1].getNumberValue(),
                parameters[2].getNumberValue()));
    }

}
