package org.d71.jrulexpr.function;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public class HourFunction extends AbstractFunction implements JrxFunction<BigDecimal> {

    @Override
    public String getToken() {
        return "HOUR";
    }

    @Override
    public BigDecimal getValue(Object... parameters) {
        return BigDecimal.valueOf(LocalTime.now().getHour());   
    }

    @Override
    public String getCronExpression() {
        return "\"0 0 * * * *\"";
    }

    @Override
    public EvaluationValue evaluate(
            Expression expression, Token functionToken, EvaluationValue... parameterValues) {
        return EvaluationValue.numberValue(getValue());   
    }
    
}
