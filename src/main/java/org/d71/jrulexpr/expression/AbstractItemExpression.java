package org.d71.jrulexpr.expression;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.d71.jrulexpr.rule.RuleUtil;
import org.d71.jrulexpr.rule.functions.HourFunction;
import org.d71.jrulexpr.rule.functions.LockFunction;
import org.d71.jrulexpr.rule.functions.MinTimeFunction;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;
import com.ezylang.evalex.parser.Token.TokenType;

public abstract class AbstractItemExpression implements IItemExpression {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();

    protected String itemName;

    protected String tagName;

    private EvaluationValueConverterIfc valueConverter = new EvaluationValueConverterIfc() {
        private EvaluationValueConverterIfc defaultConverter = new DefaultEvaluationValueConverter();

        @Override
        public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
            EvaluationValue val = null;

            if (object instanceof DecimalType)
                val = EvaluationValue.numberValue(
                        ((DecimalType) object).toBigDecimal());
            else if (object instanceof OnOffType)
                val = EvaluationValue.stringValue(
                        ((OnOffType) object).toString());
            else if (object instanceof UnDefType) {
                val = EvaluationValue.nullValue();
            } else
                val = defaultConverter.convertObject(object, configuration);

            return val;
        }
    };

    AbstractItemExpression(String itemName, String tagName) {
        this.itemName = itemName;
        this.tagName = tagName;
    }

    protected Item getItem() {
        return itemRegistry.get(itemName);
    }

    protected Optional<String> getTagValue(String tagName) {
        Optional<String> tagVal = getItem().getTags().stream()
                .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
                .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }

    protected Expression getExpression(String expression, Item item) {
        LOGGER.debug("EvalEx..");
        return new Expression(expression,
                ExpressionConfiguration.builder()
                        .evaluationValueConverter(valueConverter)
                        .build()
                        .withAdditionalFunctions(
                                Map.entry("HOUR", new HourFunction()),
                                Map.entry("LOCK", new LockFunction(RuleUtil.getMethodName(item))),
                                Map.entry("MINTIME", new MinTimeFunction(item))));
    }

    protected EvaluationValue evalXpr(String expression) throws Exception {
        LOGGER.debug("eval: " + expression);

        Expression ezyExpr = getExpression(expression, getItem());

        List<Item> items = ezyExpr.getUndefinedVariables().stream().map(v -> itemRegistry.get(v)).toList();

        if (LOGGER.isTraceEnabled()) {
            ezyExpr.getUndefinedVariables().forEach(v -> LOGGER.trace("var: " + v));

            items.forEach(i -> LOGGER
                    .trace("itm: " + i.getName() + " " + i.getState() + " " + i.getType() + " " + i.getClass()));
        }

        items.forEach(i -> ezyExpr.with(i.getName(), i.getState()));
        return ezyExpr.evaluate();
    }

    public Optional<String> getXpr() {
        return getTagValue(tagName);
    }

    public Set<Item> getXprItems() throws Exception {
        Set<Item> items;
        Optional<String> xpr = getXpr();
        if (xpr.isPresent()) {
            Expression expr = getExpression(xpr.get(), getItem());
            items = expr.getUndefinedVariables().stream()
                    .map(v -> itemRegistry.get(v)).collect(Collectors.toSet());
        } else {
            items = Collections.emptySet();
        }
        return items;
    }

    public Set<String> getUdFunctions() throws Exception {
        Optional<String> xpr = getXpr();
        return xpr.isPresent() ? new HashSet<>(getExpression(xpr.get(), getItem()).getAllASTNodes().stream()
                .map(n -> n.getToken())
                .filter(t -> t.getType() == TokenType.FUNCTION && isUd(t.getValue()))
                .map(t -> t.getValue())
                .toList()) : Collections.emptySet();
    }

    private boolean isUd(String name) {
        return "HOUR".equals(name) ||
                "LOCK".equals(name) ||
                "MINTIME".equals(name);
    }
}
