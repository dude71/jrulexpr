package org.d71.jrulexpr.rule.functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
        EvaluationValue rv;

        IItemExpression jrx = ItemExpressionFactory.getItemExpression(ItemExpressionType.JRX, item.getName());

        // t null, jrx false, rv = true, no create t
        // t null, jrx true, rv = true, create t
        // t !null, jrx false, rv = false, no reschedule
        // t !null, jrx true, rv = false, reschedule

        try {
            Duration duration = Duration
                    .ofSeconds(parameterValues.length == 1 ? parameterValues[0].getNumberValue().intValue() : 5);
            String ruleName = RuleUtil.getMethodName(item);
            JRuleTimer timer;
            synchronized (this) {
                timer = timers.get(ruleName);
            }
            boolean jrxEval = jrx.evaluate().getBooleanValue();
            LOGGER.debug("jrx: " + jrxEval);

            if (timer == null) {
                rv = EvaluationValue.booleanValue(true);
                if (jrxEval) {
                    LOGGER.debug("creating timer..");
                    timer = JRuleTimerHandler.get().createTimer(ruleName, duration, timerAction(ruleName, duration),
                            null);
                    synchronized (this) {
                        timers.put(ruleName, timer);
                    }
                    rv = EvaluationValue.booleanValue(true);
                }
            } else {
                rv = EvaluationValue.booleanValue(false);
                if (jrxEval) {
                    LOGGER.debug("rescheduling timer " + ruleName);
                    timer.rescheduleTimer(duration);
                }
            }
        } catch (Exception e) {
            if (e instanceof EvaluationException)
                throw (EvaluationException) e;
            else
                throw new RuntimeException(e);
        }

        return rv;
    }

    private Consumer<JRuleTimer> timerAction(String ruleName, Duration duration) {
        return t -> {
            boolean clear = true;
            try {
                IItemExpression jrx = ItemExpressionFactory.getItemExpression(ItemExpressionType.JRX, item.getName());
                EvaluationValue jrxEval = jrx.evaluate();
                LOGGER.debug("jrx: " + jrxEval.getValue());

                if (jrxEval.getBooleanValue()) {
                    // jrx action condition still applies, no inverse action!
                    LOGGER.debug("rescheduling timer " + ruleName + " AFTER timeout");
                    t.rescheduleTimer(duration);
                    clear = false;
                } else {
                    EvaluationValue jrxf = ItemExpressionFactory
                            .getItemExpression(ItemExpressionType.JRXF, item.getName())
                            .evaluate();
                    LOGGER.debug("Executing timer action cmd={} for {}",
                            new Object[] { jrxf.getValue(), item.getName() });
                    (new ItemCommandor(item.getName())).command(jrxf.getValue());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (clear) {
                    synchronized (this) {
                        t.cancel();
                        timers.remove(ruleName);
                    }
                }
            }
        };
    }

}