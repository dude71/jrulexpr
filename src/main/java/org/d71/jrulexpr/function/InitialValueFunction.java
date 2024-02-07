package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.expression.ItemTypeValueConverter;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.item.ValueConverter;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@FunctionParameter(name = "item", isVarArg = true)
public class InitialValueFunction extends AbstractFunction implements JrxFunction<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialValueFunction.class);
    private static final String INITIAL_VAL = "initialVal";
    public static final String JRX = "jrx";
    private JrxItem item;

    private JrxItemRegistry itemRegistry;

    private String itemName;

    @Override
    public String getToken() {
        return "INITIAL";
    }

    @Override
    public Object getValue(Object... parameters) {
        String itemName = (String)parameters[0];
        JrxItem item = itemName.equals(this.item.getName()) ? this.item : itemRegistry.getItem(itemName);
        JRuleValue state;
        Optional<String> optVal = item.getTagValue(INITIAL_VAL);
        LOGGER.trace("initialVal present: " + optVal.isPresent() + ", tags: " + item.getTags());
        if (optVal.isPresent()) {
            state = ValueConverter.convertStringToValue(optVal.get(), item.getType());
        } else {
            state = item.getState();
            if (this.item == item && state != null) {
                // initialVal for jrx item itself -> only then store initial val as tag
                item.setTagValue(INITIAL_VAL, state.stringValue());
            }
        }
        return state;
    }

    @Override
    public EvaluationValue evaluate(Expression arg0, Token arg1, EvaluationValue... parameters) throws EvaluationException {
        Object itemName = parameters.length == 0 ? item.getName() : parameters[0].getStringValue();
        LOGGER.trace("itemName: " + itemName);
        JRuleValue val = (JRuleValue) getValue(itemName);
        LOGGER.trace("value: " + val.stringValue());
        EvaluationValue eval = new EvaluationValue(val, ExpressionConfiguration.builder().evaluationValueConverter(ItemTypeValueConverter.getInstance()).build());
        return eval;
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    @Override
    public void setParameters(List<Object> values) {
        if (values.size() == 1) {
            itemName = (String) values.get(0);
        }
    }

    @Override
    public Optional<RuleTrigger> getRuleTrigger() {
        return Optional.of(new RuleTrigger() {
            @Override
            public boolean evaluateOnChange() {
                // only when itemName of function "call" equals jrx item add JRule OnUpdate trigger
                return itemName == null || itemName.equals(item.getName());
            }
            @Override
            public String getItem() {
                return itemName == null ? item.getName() : itemName;
            }
        });
    }
}
