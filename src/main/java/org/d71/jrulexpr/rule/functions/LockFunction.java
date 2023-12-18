package org.d71.jrulexpr.rule.functions;

import java.time.Duration;

import org.d71.jrulexpr.rule.ItemCommandor;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "duration")
public class LockFunction  extends AbstractFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockFunction.class);

    private String ruleName;

    public LockFunction(String ruleName) {
        this.ruleName = ruleName;
    }
    
    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        Duration dur =  
            Duration.ofSeconds(parameterValues.length == 1 ? parameterValues[0].getNumberValue().intValue() : 1);

        boolean notLocked = JRuleTimerHandler.get().getTimeLock(ruleName, dur);

        LOGGER.debug("!! " + ruleName + ": locked=" + !notLocked);

        return EvaluationValue.booleanValue(notLocked);
    }
    
}
