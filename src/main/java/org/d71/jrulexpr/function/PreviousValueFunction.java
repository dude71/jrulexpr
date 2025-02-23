package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.ValueConverter;
import org.openhab.automation.jrule.rules.value.JRuleValue;

import java.util.Optional;

import static java.time.ZonedDateTime.now;

@FunctionParameter(name = "item", isVarArg = true)
public class PreviousValueFunction extends AbstractItemChangeFunction<JRuleValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviousValueFunction.class);
    private static final String PREV_VAL = "previousVal";

    @Override
    public String getToken() {
        return "PREV";
    }

    @Override
    public JRuleValue getValue(Object... parameters) {
        String itemName = (String) parameters[0];
        JrxItem item = itemName.equals(this.item.getName()) ? this.item : itemRegistry.getItem(itemName);
        LOGGER.debug("item " + item.getName());
        JRuleValue state = item.getState();
        JRuleValue prevState;

        Optional<String> optVal = item.getTagValue(PREV_VAL);
        if (optVal.isPresent()) {
            LOGGER.debug("Prev state " + optVal.get());
            prevState = ValueConverter.convertStringToValue(optVal.get(), item.getType());
            if (this.item == item && (state == null || !state.equals(prevState))) {
                setPreviousValue(item); // set new prevVal
            }
        } else {
            prevState = null;
            if (state != null) {
                setPreviousValue(item); // set prevVal
            }
        }
        return prevState;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        // 0 = item name (optional)
        return EvaluationValue.of(getValue(parameters.length == 0 ? item.getName() : parameters[0].getStringValue()), getExpressionConfig());
    }

    private void setPreviousValue(JrxItem item) {
        if (this.item == item) {
            JRuleValue state = item.getState();
            String value = state == null ? null : state.stringValue();
            if (value == null) {
                LOGGER.debug("remove tag " + PREV_VAL);
                item.removeTag(PREV_VAL);
            } else {
                LOGGER.debug("set tag " + PREV_VAL + " to " + value);
                item.setTagValue(PREV_VAL, value);
            }
            long epochMilli = now().toInstant().toEpochMilli();
        }
    }
}
