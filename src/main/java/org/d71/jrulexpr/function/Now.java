package org.d71.jrulexpr.function;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public class Now extends AbstractFunction implements JrxFunction<Long> {

    @Override
    public EvaluationValue evaluate(Expression arg0, Token arg1, EvaluationValue... arg2) throws EvaluationException {
        return EvaluationValue.numberValue(BigDecimal.valueOf(getValue()));
    }

    @Override
    public String getToken() {
        return "NOW";
    }

    @Override
    public Long getValue(Object... parameters) {
        return ZonedDateTime.now().toInstant().toEpochMilli();
    }
    
}
