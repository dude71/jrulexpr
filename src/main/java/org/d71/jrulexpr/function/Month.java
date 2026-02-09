package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Month extends AbstractFunction implements JrxFunction<BigDecimal> {
    @Override
    public String getToken() {
        return "MONTH";
    }

    @Override
    public BigDecimal getValue(Object... parameters) {
        return BigDecimal.valueOf(LocalDate.now().getMonthValue());
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) throws EvaluationException {
        return EvaluationValue.numberValue(getValue());
    }
}
