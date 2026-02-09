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
import java.util.Set;

@FunctionParameter(name = "item", isVarArg = true)
public class ChangeAge extends AbstractItemChangeFunction<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeAge.class);

    @Override
    public String getToken() {
        return "CHAGE";
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
        Long chAge = lastCh == null ? null : (ZonedDateTime.now().toInstant().toEpochMilli() - lastCh) / 1000;
        LOGGER.debug("item: {}, lastCh: {}, chAge: {}", item.getName(), lastCh, chAge);
        return chAge;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        // 0 = item name (optional)
        return EvaluationValue.of(getValue(parameters.length == 0 ? item.getName() : parameters[0].getStringValue()), getExpressionConfig());
    }

}
