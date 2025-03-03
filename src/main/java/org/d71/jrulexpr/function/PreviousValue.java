package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.ValueConverter;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openhab.automation.jrule.rules.value.JRuleValue;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.time.ZonedDateTime.now;

@FunctionParameter(name = "item", isVarArg = true)
public class PreviousValue extends AbstractItemChangeFunction<JRuleValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviousValue.class);

    @Override
    public String getToken() {
        return "PREV";
    }

    @Override
    public Set<RuleTrigger> getRuleTriggers() {
        return Collections.emptySet();
    }

    @Override
    public JRuleValue getValue(Object... parameters) {
        String itemName = (String) parameters[0];
        JrxItem item = itemName.equals(this.item.getName()) ? this.item : itemRegistry.getItem(itemName);
        JRuleValue prevState = item.getPreviousState();
        LOGGER.debug("item: {}, state: {}, prev: {}", item.getName(), item.getState(), prevState );
        return prevState;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        // 0 = item name (optional)
        return EvaluationValue.of(getValue(parameters.length == 0 ? item.getName() : parameters[0].getStringValue()), getExpressionConfig());
    }
}
