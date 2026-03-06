package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfTriggered  extends AbstractItemChangeFunction<Boolean>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTriggered.class);

    @Override
    public String getToken() {
        return "SELFTRIG";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        LOGGER.debug(("item: " + item.getName()));
        return selfTriggered();
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) throws EvaluationException {
        Boolean value = getValue();
        LOGGER.debug("eval: " + value);
        return EvaluationValue.booleanValue(value);
    }
}
