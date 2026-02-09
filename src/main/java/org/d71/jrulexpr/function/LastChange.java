package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@FunctionParameter(name = "item", isVarArg = true)
public class LastChange extends AbstractItemChangeFunction<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LastChange.class);

    @Override
    public String getToken() {
        return "LASTCH";
    }

    @Override
    public Set<RuleTrigger> getRuleTriggers() {
        return Collections.emptySet();
    }

    @Override
    public Long getValue(Object... parameters) {
        String itemName = (String) parameters[0];
        JrxItem item = itemName.equals(this.item.getName()) ? this.item : itemRegistry.getItem(itemName);
        Long lastCh = item.getLastUpdated();
        LOGGER.debug("item: " + item.getName() + " lastCh: " + lastCh);
        return lastCh;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        // 0 = item name (optional)
        return EvaluationValue.of(getValue(parameters.length == 0 ? item.getName() : parameters[0].getStringValue()), getExpressionConfig());
    }

}
