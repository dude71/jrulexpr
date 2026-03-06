package org.d71.jrulexpr.function;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.rule.RuleTrigger;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@FunctionParameter(name = "trigger", isVarArg = true)
public class Minute extends AbstractFunction implements JrxFunction<BigDecimal> {
    private boolean trigger = true;

    @Override
    public String getToken() {
        return "MINUTE";
    }

    @Override
    public Set<RuleTrigger> getRuleTriggers() {
        return trigger ? Collections.singleton(new RuleTrigger() {
            public String getCronExpression() {
                return "0 * * * * *";
            }
        }) : Collections.emptySet();
    }

    @Override
    public void setParameters(List<Object> values) {
        if (!values.isEmpty()) {
            trigger = Boolean.parseBoolean((String)values.get(0));
        }
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
