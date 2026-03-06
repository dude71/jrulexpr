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
import org.slf4j.event.Level;

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
        // t !null, jrx false, rv = false, no reschedule
        // t !null, jrx true, rv = false, reschedule
        // t.expired, jrx true, reschedule
        // t.expired, jrx false, no reschedule, itm=jrxf 

        try {
            Duration duration = Duration
                    .ofSeconds(parameters.length == 1 ? ((Number) parameters[0]).longValue() : 5L);
            String ruleName = item.getRuleMethodName();
            JRuleTimer timer;

            Boolean jrxEval = item.evaluateJrx();
            LOGGER.trace("MinTime.getValue: " + item.getName() + ", [jrx]: " + item.getJrx() + " -> " + jrxEval);

            timer = timers.get(ruleName);

            rv = timer == null || timer.isDone();

            if (rv) {
                if (timer != null) {
                    logTimerMsg(Level.INFO, "getValue", timer, "cancelling timer!");
                    cancelTimer(timer);
                }
                if (jrxEval) {
                    JRuleTimerHandler timerHandler = JRuleTimerHandler.get();
                    synchronized(timerHandler) {
                        // The JRuleTimerHandler is somehow not a synchronized method, manually synchronize on the singleton (eff. same as sync method)
                        timer = timerHandler.createTimer(ruleName, duration, timerAction(ruleName, duration), null);
                    }
                    timers.put(ruleName, timer);
                    logTimerMsg(Level.DEBUG, "getValue", timer, "created timer (duration=" + duration + ", #timers=" + timers.size() + ")");
                }
            } else {
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

        LOGGER.trace("trigger {}, evaluate with param {}", new Object[] { item.getLastTriggeredBy(), (parameterValues.length > 0 ? parameterValues[0] : "[]") });

        Object[] param = parameterValues.length > 0 ? new Number[] { parameterValues[0].getNumberValue() }
                : new Number[0];

        return EvaluationValue.booleanValue(getValue(param));
    }

    private String timerName(JRuleTimer t) {
        return t.getLogName().replaceFirst("^.* / ", "") + "/" + t.hashCode();
    }

    private void logTimerMsg(Level level, String method, JRuleTimer timer, String msg) {
        String logMsg = "{}/{} {}/done={}: {}";
        Object[] param = new Object[] {System.identityHashCode(this), method, timerName(timer), timer.isDone(), msg};
        switch (level) {
            case INFO :
                LOGGER.info(logMsg, param);
                break;
            case WARN:
                LOGGER.warn(logMsg, param);
                break;
            case DEBUG:
                LOGGER.debug(logMsg, param);
                break;                
            case ERROR:
                LOGGER.error(logMsg, param);
                break;
            case TRACE:
                LOGGER.trace(logMsg, param);
                break;                                
        }
    }

    private void rescheduleTimer(String ruleName, JRuleTimer timer, Duration duration) {
        JRuleTimer newTimer = timer.rescheduleTimer(duration); // is synchronized, does cancelTimer (by ruleName) internally
        JRuleTimer oldTimer = timers.put(ruleName, newTimer);
        String oldHC = (oldTimer == null ? "<undef>" : "" + oldTimer.hashCode());
        logTimerMsg(Level.DEBUG, "rescheduleTimer", timer, "oldTimer=" +  oldHC + ", newTimer=" + newTimer.hashCode());
        if (!timer.isDone()) {
            logTimerMsg(Level.INFO, "rescheduleTimer", timer, "cancelled timer (old=" + oldHC + ", new=" + newTimer.hashCode() + ") not done!");     
            timer.cancel();
        }
    }

    private void cancelTimer(JRuleTimer timer) {
        String ruleName = item.getRuleMethodName();
        int t = timers.size();
        boolean cancelled = JRuleTimerHandler.get().cancelTimer(ruleName); // is synchronized
        JRuleTimer removed = timers.remove(ruleName);
        logTimerMsg(Level.DEBUG, "cancelTimer", timer, "item state: " + item.getState() + ", timers before: " + t + ", after: " + timers.size() + ", cancelled: " + cancelled + ", removed: " + (removed == null ? "NONE" : ""+removed.hashCode()));
        if (removed != null && !removed.isDone()) {
            logTimerMsg(Level.INFO, "cancelTimer", timer, "cancelled timer not done!");
            timer.cancel();
        }
    }

    private Consumer<JRuleTimer> timerAction(String ruleName, Duration duration) {
        return t -> {
            logTimerMsg(Level.DEBUG, "timerAction", t, "item state=" + item.getState());

            boolean clear = false;

            try {
                Boolean jrx = item.evaluateJrx();
                if (jrx) {
                    // jrxp met and jrx action condition still applies
                    logTimerMsg(Level.DEBUG, "timerAction", t, "rescheduling AFTER timeout, item state=" + item.getState());
                    rescheduleTimer(ruleName, t, duration);
                } else {
                    clear = true;
                    JRuleValue newState = item.evaluateJrxf();
                    logTimerMsg(Level.DEBUG, "timerAction", t, "oldState=" + item.getState() + "newState=" + newState);
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
