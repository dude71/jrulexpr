package org.d71.jrulexpr.function;

import java.time.Duration;

import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "duration")
public class LockFunction extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockFunction.class);

    private String ruleName;

    @Override
    public final String getToken() {
        return "LOCK";
    }

    @Override
    public void setItem(JrxItem item) {
        this.ruleName = item.getRuleMethodName();
    }

    @Override
    public Boolean getValue(Object... parameters) {
        Duration duration = Duration.ofSeconds((int)parameters[0]);
        return JRuleTimerHandler.get().getTimeLock(ruleName, duration);
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {

        boolean gotLock = getValue(parameterValues.length == 1 ? parameterValues[0].getNumberValue().intValue() : 1);

        LOGGER.debug(ruleName + ": got lock=" + gotLock);

        return EvaluationValue.booleanValue(gotLock);
    }

    
}
