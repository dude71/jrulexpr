package org.d71.jrulexpr.function;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.rule.RuleTrigger;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

public class MinuteFunction  extends AbstractFunction implements JrxFunction<BigDecimal> {
    @Override
    public String getToken() {
        return "MINUTE";
    }

    @Override
    public Optional<RuleTrigger> getRuleTrigger() {
        return Optional.of(new RuleTrigger() {
            public String getCronExpression() {
                return "\"0 * * * * *\"";
            }
        });
    }

    @Override
    public BigDecimal getValue(Object... parameters) {
        return BigDecimal.valueOf(LocalTime.now().getMinute());
    }

    @Override
    public EvaluationValue evaluate(
            Expression expression, Token functionToken, EvaluationValue... parameterValues) {
        return EvaluationValue.numberValue(getValue());
    }
}
