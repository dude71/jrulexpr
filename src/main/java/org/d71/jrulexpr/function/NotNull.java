package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@FunctionParameter(name = "item1")
@FunctionParameter(name = "item2")
@FunctionParameter(name = "item3", isVarArg = true)
public class NotNull extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotNull.class);

    @Override
    public String getToken() {
        return "NN";
    }

    @Override
    public Boolean getValue(Object... values) {
        boolean rv = false;
        for (Object value : values) {
            rv = value != null;
            if (!rv) {
                break;
            }
        }
        return rv;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        Object[] itemValues = Arrays.stream(parameters).map(EvaluationValue::getValue).toList().toArray(Object[]::new);
        return EvaluationValue.booleanValue(getValue(itemValues));
    }

}
