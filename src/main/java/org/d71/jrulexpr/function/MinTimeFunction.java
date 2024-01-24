package org.d71.jrulexpr.function;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler.JRuleTimer;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "duration")
public class MinTimeFunction extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinTimeFunction.class);
    private static Map<String, JRuleTimer> timers = new HashMap<>();

    private JrxItem item;

    @Override
    public String getToken() {
        return "MINTIME";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        Boolean rv;

        // t null, jrx false, rv = true, no create t
        // t null, jrx true, rv = true, create t
        // t !null, t.expired, rv = true, no create t
        // t !null, jrx false, rv = false, no reschedule
        // t !null, jrx true, rv = false, reschedule

        try {
            Duration duration = Duration
                    .ofSeconds(parameters.length == 1 ? ((Number) parameters[0]).longValue() : 5l);
            String ruleName = item.getRuleMethodName();
            JRuleTimer timer;

            synchronized (timers) {
                timer = timers.get(ruleName);

                if (timer != null && timer.isDone()) { // call from timer function (done)
                    LOGGER.debug("timer {} expired!", new Object[] { timerName(timer) });
                    return true;
                }
            }

            Boolean jrxEval = item.evaluateJrx();
            LOGGER.debug(item.getName() + ", jrx: " + item.getJrx() + " -> " + jrxEval);

            rv = timer == null;

            if (rv) {
                if (jrxEval) {
                    synchronized (timers) {
                        timer = JRuleTimerHandler.get().createTimer(ruleName, duration, timerAction(ruleName, duration),
                                null);
                        timers.put(ruleName, timer);
                        LOGGER.debug("created timer {}, #timers {}", new Object[]{timerName(timer), timers.size()});
                    }
                }
            } else {
                LOGGER.debug("timer {}, done: {}, running: {}",
                        new Object[] { timerName(timer), timer.isDone(), timer.isRunning() });
                if (jrxEval) {
                    rescheduleTimer(ruleName, timer, duration);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return rv;
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {

        LOGGER.debug("trigger {}, evaluate with param {}", new Object[] { item.getLastTriggeredBy(), (parameterValues.length > 0 ? parameterValues[0] : "[]") });

        Object[] param = parameterValues.length > 0 ? new Number[] { parameterValues[0].getNumberValue() }
                : new Number[0];

        return EvaluationValue.booleanValue(getValue(param));
    }

    private String timerName(JRuleTimer t) {
        return t.getLogName() + " " + t.hashCode();
    }

    private void rescheduleTimer(String ruleName, JRuleTimer timer, Duration duration) {
        String timerName = timerName(timer);
        synchronized (timers) {
            JRuleTimer oldTimer = timers.get(ruleName);
            JRuleTimer newTimer = timer.rescheduleTimer(duration);
            oldTimer.cancel();
            LOGGER.trace("timer {}, oldTimer {}, newTimer {}", new Object[] {timer.hashCode(), oldTimer.hashCode(), newTimer.hashCode()});
            timers.put(ruleName, newTimer);
            LOGGER.debug("rescheduled timer {} -> {}, #timers {}", new Object[] { timerName, newTimer.hashCode(), timers.size() });
        }
    }

    private Consumer<JRuleTimer> timerAction(String ruleName, Duration duration) {
        return t -> {
            boolean clear = false;

            // jrxp true, jrx true -> resched, nop
            // jrxp true, jrx false -> stop, set new state
            // jrxp false -> stop, nop

            try {
                // Optional<State> newState = item.evaluateNewState();
                Boolean jrxp = item.evaluateJrxp();

                LOGGER.trace("ta: timer {}, item {}, state {}, jrxp {}={}",
                        new Object[] { timerName(t), item.getName(), item.getState(), item.getJrxp(), jrxp });

                if (jrxp) {
                    Boolean jrx = item.evaluateJrx();
                    if (jrx) {
                        // jrxp met and jrx action condition still applies
                        LOGGER.debug("ta: rescheduling timer {}, done={}, running={} AFTER timeout", new Object[] {timerName(t), t.isDone(), t.isRunning()});
                        rescheduleTimer(ruleName, t, duration);
                    } else {
                        clear = true;
                        State newState = item.evaluateJrxf();
                        LOGGER.debug("ta: timer {} action cmd={} for {}",
                                new Object[] { timerName(t), newState, item.getName() });
                        item.send(newState);
                    }
                } else {
                    clear = true;
                    LOGGER.warn("ta: ending timer {}, jrxp={} without action!", new Object[] { timerName(t), item.getJrxp() });
                }
            } finally {
                if (clear) {
                    synchronized (timers) {
                        t.cancel();
                        timers.remove(ruleName);
                        LOGGER.debug("ta: timer {} removed, #timers {}", new Object[]{timerName(t), timers.size()});
                    }
                }
            }
        };
    }
}