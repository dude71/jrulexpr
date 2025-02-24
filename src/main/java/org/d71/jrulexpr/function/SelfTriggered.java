package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class SelfTriggered  extends AbstractFunction implements JrxFunction<Boolean> {
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
        return event instanceof JRuleItemEvent itemEvent && item.equals(itemEvent.getItem());
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... evaluationValues) throws EvaluationException {
        return EvaluationValue.booleanValue(getValue());
    }
}
