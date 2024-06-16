package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FunctionParameter(name = "swItemName")
@FunctionParameter(name = "swOverrideItemName", isVarArg = true)
public class EnabledFunction extends AbstractFunction implements JrxFunction<Boolean> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private Set<String> params;

    private JrxItemRegistry itemRegistry;

    @Override
    public String getToken() {
        return "ENABLED";
    }

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    @Override
    public Boolean getValue(Object... parameters) {
        JrxItem swItem = itemRegistry.getItem((String)parameters[0]);
        LOGGER.debug("swItem {}", swItem);
        JrxItem swOverrideItem = parameters.length > 1 ? itemRegistry.getItem((String)parameters[1]) : null;
        LOGGER.debug(swItem + " " + swOverrideItem);
        return swItem.stateEquals(JRuleOnOffValue.ON) && (swOverrideItem == null || !swOverrideItem.stateEquals(JRuleOnOffValue.ON));
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameterValues) throws EvaluationException {
        LOGGER.debug("evaluating " + expression.toString() + "..");
        Object[] params = Arrays.stream(parameterValues).map(EvaluationValue::getStringValue).toArray();
        return EvaluationValue.booleanValue(getValue(params));
    }

    @Override
    public void setParameters(List<Object> values) {
        LOGGER.debug("setting " + values.size() + " params");
        params = values.stream().map(p -> (String)p).collect(Collectors.toSet());
    }

    @Override
    public Set<RuleTrigger> getRuleTriggers() {
        return params.stream().map(i -> new RuleTrigger() {
            @Override
            public String getItemName() {
                return i;
            }

            @Override
            public boolean evaluateOnUpdate() {
                return true;
            }
        }).collect(Collectors.toSet());
    }

}
