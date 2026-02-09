package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class TriggeredBy extends AbstractFunction implements JrxFunction<String> {
    private JrxItem item;

    @Override
    public String getToken() {
        return "TRIGGEREDBY";
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public String getValue(Object... parameters) {
        JRuleEvent event = item.getLastTriggeredBy();
        return event instanceof JRuleItemEvent itemEvent ? itemEvent.getItem().getName() : event.getClass().getSimpleName();
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        return EvaluationValue.stringValue(getValue());
    }

}