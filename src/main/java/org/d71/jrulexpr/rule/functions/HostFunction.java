package org.d71.jrulexpr.rule.functions;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "hostOrIp")
public class HostFunction  extends AbstractFunction {

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) throws EvaluationException {
        return null;
    }
}
