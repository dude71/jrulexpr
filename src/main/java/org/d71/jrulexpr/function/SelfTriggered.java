package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfTriggered  extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTriggered.class);
    private JrxItem item;

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public String getToken() {
        return "SELFTRIG";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        JRuleEvent event = item.getLastTriggeredBy();
        LOGGER.debug(("item: " + item.getName()));
        if (event instanceof JRuleItemEvent itemEvent) {
            LOGGER.debug("evt item: " + itemEvent.getItem().getName());
        }
        return event instanceof JRuleItemEvent itemEvent && item.getName().equals( itemEvent.getItem().getName());
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) throws EvaluationException {
        Boolean value = getValue();
        LOGGER.debug("eval: " + value);
        return EvaluationValue.booleanValue(value);
    }
}
