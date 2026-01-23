package org.d71.jrulexpr.function;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.internal.handler.JRuleTimerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public abstract class AbstractItemLockFunction  extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractItemLockFunction.class);

    private static final String LOCK_TS_E = "_LOCK_TS_E";

    private JrxItem item;
    private String ruleName;

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
        ruleName = item.getRuleMethodName();
    }
    
    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        boolean gotLock = getValue(parameterValues.length == 1 ? parameterValues[0].getNumberValue().intValue() : 1);

        LOGGER.debug(ruleName + ": got lock=" + gotLock);

        return EvaluationValue.booleanValue(gotLock);
    }

    protected boolean lock(Duration duration) {
        boolean rv = false;
        JRuleTimerHandler timerHandler = JRuleTimerHandler.get();
        synchronized(timerHandler) {
            LOGGER.debug("lock " + duration);
            rv = timerHandler.getTimeLock(ruleName, duration);
            if (rv) {
                ZonedDateTime endLock = ZonedDateTime.now().plus(duration);
                item.setTagValue(LOCK_TS_E, String.valueOf(endLock.toEpochSecond()));
            }
        }
        return rv;
    }

    protected boolean extendLock(Duration duration) {
        boolean rv = false;
        Duration calcDuration = null;
        JRuleTimerHandler timerHandler = JRuleTimerHandler.get();

        synchronized (timerHandler) {
            if (timerHandler.isTimeLocked(ruleName)) {
                rv = true;
                LOGGER.debug("timeLocked");
                String tag = item.getTagValue(LOCK_TS_E).orElse("0");
                LOGGER.debug("LOCK_TS_E " + tag);
                long endLock = Long.parseLong(tag);
                long now = ZonedDateTime.now().toEpochSecond();
                LOGGER.debug("now " + now);
                long lockSecLeft = endLock - now;
                LOGGER.debug("lockSecLeft " + lockSecLeft);
                if (lockSecLeft > 0 && lockSecLeft < duration.toSeconds()) {
                    calcDuration = duration.plusSeconds(lockSecLeft);
                    timerHandler.cancelTimer(JRuleTimerHandler.LOCK_PREFIX + ruleName);
                }
            } else {
                LOGGER.debug("NOT timelocked");
            }
        }

        if (calcDuration != null) {
            LOGGER.debug("extending lock " + calcDuration.getSeconds() + "s");
            rv = lock(calcDuration);
        }

        return rv;
    }
    
}
