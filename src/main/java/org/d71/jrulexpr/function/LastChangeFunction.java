package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Optional;

@FunctionParameter(name = "item", isVarArg = true)
public class LastChangeFunction extends AbstractItemChangeFunction<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LastChangeFunction.class);
    private static final String CHANGE_EPOCH = "changeEpoch";

    @Override
    public String getToken() {
        return "LASTCHANGE";
    }

    @Override
    public Long getValue(Object... parameters) {
        String itemName = (String) parameters[0];
        JrxItem item = itemName.equals(this.item.getName()) ? this.item : itemRegistry.getItem(itemName);
        Long lastCh;
        LOGGER.debug("item: " + item.getName());

        Optional<String> optEp = item.getTagValue(CHANGE_EPOCH);
        if (optEp.isPresent()) {
            LOGGER.debug("Last ch: " + optEp.get());
            lastCh = Long.parseLong(optEp.get());
            setLastChange(item);
        } else {
            lastCh = null;
            setLastChange(item);
        }
        return lastCh;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        // 0 = item name (optional)
        return EvaluationValue.of(getValue(parameters.length == 0 ? item.getName() : parameters[0].getStringValue()), getExpressionConfig());
    }

    private void setLastChange(JrxItem item) {
        if (this.item == item) {
            JRuleEvent evt = item.getLastTriggeredBy();
            if (evt instanceof JRuleItemEvent && ((JRuleItemEvent)evt).getItem().equals(item)) {
                String ep = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
                LOGGER.debug("set tag: " + CHANGE_EPOCH + " to " + ep);
                item.setTagValue(CHANGE_EPOCH, ep);
            }
        }
    }
}
