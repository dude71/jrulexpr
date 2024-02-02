package org.d71.jrulexpr.function;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token;

public class GroupFunction extends AbstractFunction implements JrxFunction<Boolean> { // group change triggered
    private JrxItem item;

    private String groupName;

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public Boolean getValue(Object... parameters) {
        if (parameters.length == 0) {
            return false;
        } else {
            String groupName = (String) parameters[0];
            JRuleItemEvent evt = item.getLastTriggeredBy() instanceof JRuleItemEvent ? (JRuleItemEvent) item.getLastTriggeredBy() : null;
            return evt != null && groupName.equals(evt.getItem().getName());
        }
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) throws EvaluationException {
        Object[] param = parameterValues.length > 0 ? new String[] { parameterValues[0].getStringValue() } : new String[0];
        return EvaluationValue.booleanValue(getValue(param));
    }

    @Override
    public String getToken() {
        return "GROUP";
    }

    @Override
    public void setParameters(List<Object> values) {
        if (values.size() == 1) {
            groupName = (String)values.get(0);
        }
    }

    @Override
    public Optional<RuleTrigger> getRuleTrigger() {
        return groupName == null ? Optional.empty() : Optional.of(new RuleTrigger() {
            @Override
            public Set<String> getGroups() {
                return Collections.singleton(groupName);
            }
        });
    }
    
}
