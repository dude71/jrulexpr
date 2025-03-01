package org.d71.jrulexpr.function;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler.JRuleTimer;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "duration")
public class MinTime extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinTime.class);
    private static final Map<String, JRuleTimer> timers = Collections.synchronizedMap(new HashMap<>());

    private JrxItem item;

    // keep ref to ClassLoader so that Class itself does not get unloaded!
    private Object loader = MinTime.class.getClassLoader();

    @Override
    public String getToken() {
        return "MINTIME";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        boolean rv;

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

            Boolean jrxEval = item.evaluateJrx();
            LOGGER.debug("MinTime.getValue: " + item.getName() + ", [jrx]: " + item.getJrx() + " -> " + jrxEval);

            timer = timers.get(ruleName);

            rv = timer == null;

            if (rv) {
                if (jrxEval) {
                    timer = JRuleTimerHandler.get().createTimer(ruleName, duration, timerAction(ruleName, duration),
                            null);
                    timers.put(ruleName, timer);
                    LOGGER.debug("(" + System.identityHashCode(this) + "/" + System.identityHashCode(timers) + ") created timer {}, #timers {}", new Object[]{timerName(timer), timers.size()});
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

    @Override
    protected void finalize() throws Throwable {
        LOGGER.trace("Cleanup (" + System.identityHashCode(this) + "/" + System.identityHashCode(timers) + "), item: " + item.getName());
        timers.values().forEach(this::cancelTimer);
        timers.clear();
        super.finalize();
    }

    private String timerName(JRuleTimer t) {
        return t.getLogName().replaceFirst("^.* / ", "") + " " + t.hashCode();
    }

    private void rescheduleTimer(String ruleName, JRuleTimer timer, Duration duration) {
        String timerName = timerName(timer);
        JRuleTimer newTimer = timer.rescheduleTimer(duration);
        LOGGER.trace("timer {}, oldTimer {}, newTimer {}", new Object[] {timer.hashCode(), timer.hashCode(), newTimer.hashCode()});
        JRuleTimer oldTimer = timers.put(ruleName, newTimer);
        LOGGER.debug("(" + System.identityHashCode(this) + "/" + System.identityHashCode(timers) + ") rescheduled timer {} (done={}) -> {} (was {}), #timers {}", new Object[] { timerName, timer.isDone(), newTimer.hashCode(), oldTimer == null ? 0 : oldTimer.hashCode(), timers.size() });
        if (!timer.isDone()) {
            LOGGER.warn("Killing rescheduled timer: " + timerName);
            cancelTimer(timer);
        }
    }

    private void cancelTimer(JRuleTimer timer) {
        String ruleName = item.getRuleMethodName();
        int t = timers.size();
        boolean cancelled = JRuleTimerHandler.get().cancelTimer(ruleName);
        timer.cancel();
        boolean removed = timers.remove(ruleName) != null;
        LOGGER.debug("cancelTimer: (" + System.identityHashCode(this) + "/" + System.identityHashCode(timers) + ") " + timer.getLogName() + ", state: " + item.getState() + ", timers before: " + t + ", after: " + timers.size() + ", cancelled: " + cancelled + ", removed: " + removed);
    }

    private Consumer<JRuleTimer> timerAction(String ruleName, Duration duration) {
        return t -> {
            LOGGER.trace("ta: timer {}, item {}, state {}",
                    new Object[] { timerName(t), item.getName(), item.getState() });

            boolean clear = false;

            try {
                Boolean jrx = item.evaluateJrx();
                if (jrx) {
                    // jrxp met and jrx action condition still applies
                    LOGGER.debug("ta: rescheduling timer {}, state={}, done={}, running={} AFTER timeout", new Object[]{timerName(t), item.getState(), t.isDone(), t.isRunning()});
                    rescheduleTimer(ruleName, t, duration);
                } else {
                    clear = true;
                    JRuleValue newState = item.evaluateJrxf();
                    LOGGER.debug("ta: timer {} action oldState={} newState={} for {}",
                            new Object[]{timerName(t), item.getState(), newState, item.getName()});
                    item.send(newState);
                }
            } finally {
                if (clear) {
                    cancelTimer(t);
                }
            }
        };
    }
}
