package org.d71.jrulexpr.rule.functions;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public class HourFunction extends AbstractFunction {
    @Override
    public EvaluationValue evaluate(
            Expression expression, Token functionToken, EvaluationValue... parameterValues) {
        return EvaluationValue.numberValue(BigDecimal.valueOf(LocalTime.now().getHour()));
    }
}