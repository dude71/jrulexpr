package org.d71.jrulexpr.rule.functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.d71.jrulexpr.expression.IItemExpression;
import org.d71.jrulexpr.expression.ItemExpressionFactory;
import org.d71.jrulexpr.expression.ItemExpressionType;
import org.d71.jrulexpr.rule.ItemCommandor;
import org.d71.jrulexpr.rule.RuleUtil;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler.JRuleTimer;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.openhab.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionParameter(name = "duration")
public class MinTimeFunction extends AbstractFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinTimeFunction.class);
    private static Map<String, JRuleTimer> timers = new HashMap<>();

    private Item item;

    public MinTimeFunction(Item item) {
        this.item = item;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        EvaluationValue rv = EvaluationValue.booleanValue(false);

        IItemExpression jrx = ItemExpressionFactory.getItemExpression(ItemExpressionType.JRX, item.getName());

        LOGGER.debug("jrx: " + jrx);

        try {
            if (jrx.evaluate().getBooleanValue()) {
                Duration duration = parameterValues[0].getDurationValue();
                String ruleName = RuleUtil.getMethodName(item);
                JRuleTimer timer = timers.get(ruleName);
                if (timer == null) {
                    LOGGER.debug("creating timer..");
                    EvaluationValue jrxf = ItemExpressionFactory.getItemExpression(ItemExpressionType.JRXF, item.getName()).evaluate();
                    timer = JRuleTimerHandler.get().createTimer(ruleName, duration, t -> {
                        try {
                            (new ItemCommandor(item.getName())).command(jrxf.getValue());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            t.cancel();
                            timers.remove(ruleName);
                        }
                    }, null);
                    timers.put(ruleName, timer);
                    EvaluationValue.booleanValue(true);
                } else {
                    LOGGER.debug("rescheduling timer " + ruleName);
                    timer.rescheduleTimer(duration);
                }
            }
        } catch (Exception e) {
            if (e instanceof EvaluationException) throw (EvaluationException)e;
            throw new RuntimeException(e);
        }

        return rv;
    }
    
}

/*
 * MINTIME(timeout) only in jrxp
 *
 * 1a. jrule triggered, jrxp with MINTIME(timeout)
 * 1b. jrxp eval = timer exits ? (jrx eval ? timer.timeout=timeleft+timeout : nop), false : new timer (with inverse action jrxf), true
 * 1c. jrx eval when jrxp true, do action
 * 2a. timer timeout
 * 2b. do inverse, timer = null
 */