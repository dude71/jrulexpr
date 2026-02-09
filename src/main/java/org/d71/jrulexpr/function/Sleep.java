package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "millis")
public class Sleep extends AbstractFunction implements JrxFunction<Boolean> {

    @Override
    public String getToken() {
        return "SLEEP";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        long sleepMs = ((Number) parameters[0]).longValue();
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        return EvaluationValue.booleanValue(getValue(new Object[] { parameters[0].getNumberValue() }));
    }

}
