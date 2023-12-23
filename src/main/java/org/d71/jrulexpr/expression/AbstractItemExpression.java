package org.d71.jrulexpr.expression;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.parser.Token.TokenType;
import org.d71.jrulexpr.rule.functions.*;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractItemExpression implements IItemExpression {

    private static final Map<String, Class<? extends FunctionIfc>> UD_FUNCTIONS = Map.of(
            "HOST", HostFunction.class,
            "HOUR", HourFunction.class,
            "MINTIME", MinTimeFunction.class,
            "LOCK", LockFunction.class);

    private final EvaluationValueConverterIfc valueConverter = new EvaluationValueConverterIfc() {
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

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();

    protected String itemName;

    protected String tagName;

    AbstractItemExpression(String itemName, String tagName) {
        this.itemName = itemName;
        this.tagName = tagName;
    }

    public Optional<String> getXpr() {
        return getTagValue();
    }

    public Set<Item> getXprItems() throws Exception {
        Set<Item> items;
        Optional<String> xpr = getXpr();
        if (xpr.isPresent()) {
            Expression expr = getExpression(xpr.get());
            items = getXprItems(expr);
        } else {
            items = Collections.emptySet();
        }
        return items;
    }

    public Set<String> getXprFunctions() throws Exception {
        Optional<String> xpr = getXpr();
        return xpr.isPresent() ? new HashSet<>(getExpression(xpr.get()).getAllASTNodes().stream()
                .map(n -> n.getToken())
                .filter(t -> t.getType() == TokenType.FUNCTION && isUd(t.getValue()))
                .map(t -> t.getValue())
                .toList()) : Collections.emptySet();
    }

    public EvaluationValue evaluate() throws Exception {
        EvaluationValue rv;
        Optional<String> xpr = getXpr();
        if (xpr.isPresent()) {
            rv = evalXpr(xpr.get());
            validateXprValue(rv);
        } else {
            rv = getDefault();
        }
        return rv;
    }

    protected EvaluationValue getDefault() {
        return EvaluationValue.nullValue();
    }

    protected void validateXprValue(EvaluationValue value) {
    }

    protected Item getItem() {
        return itemRegistry.get(itemName);
    }

    private Set<Item> getXprItems(Expression expression) throws Exception {
        Set<Item> items = expression.getUndefinedVariables().stream()
                .map(v -> itemRegistry.get(v)).collect(Collectors.toSet());

        if (LOGGER.isTraceEnabled()) {
            expression.getUndefinedVariables().forEach(v -> LOGGER.trace("var: " + v));

            items.forEach(i -> LOGGER
                    .trace("itm: " + i.getName() + " " + i.getState() + " " + i.getType() + " " + i.getClass()));
        }

        return items;
    }

    private EvaluationValue evalXpr(String xpr) throws Exception {
        Expression ezyExpr = getExpression(xpr);

        LOGGER.debug("eval: " + xpr);

        Set<Item> items = getXprItems(ezyExpr);

        items.forEach(i -> ezyExpr.with(i.getName(), i.getState()));
        return ezyExpr.evaluate();
    }

    private Expression getExpression(String xpr) {
        LOGGER.debug("EvalEx..");

        return new Expression(xpr,
                ExpressionConfiguration.builder()
                        .evaluationValueConverter(valueConverter)
                        .build()
                        .withAdditionalFunctions(getUdFunctions(xpr)));
    }

    private Map.Entry<String, FunctionIfc>[] getUdFunctions(String xpr) {
        Item item = getItem();

        Set<Map.Entry<String, FunctionIfc>> itmFunctions = UD_FUNCTIONS.entrySet().stream()
                .filter(e -> xpr.contains(e.getKey()) && AbstractItemFunction.class.isAssignableFrom(e.getValue()))
                .map(e -> {
                    try {
                        return Map.entry(e.getKey(),
                                (FunctionIfc) e.getValue().getDeclaredConstructor(Item.class).newInstance(item));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toSet());

        Set<String> itmFuncSet = itmFunctions.stream().map(f -> f.getKey()).collect(Collectors.toSet());

        Set<Map.Entry<String, FunctionIfc>> functions = UD_FUNCTIONS.entrySet().stream()
                .filter(e -> xpr.contains(e.getKey()) && !itmFuncSet.contains(e.getKey())
                        && AbstractFunction.class.isAssignableFrom(e.getValue()))
                .map(e -> {
                    try {
                        return Map.entry(e.getKey(), (FunctionIfc) e.getValue().getDeclaredConstructor().newInstance());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toSet());

        Set<Map.Entry<String, FunctionIfc>> allFunctions = new HashSet<>();
        allFunctions.addAll(functions);
        allFunctions.addAll(itmFunctions);

        Map.Entry<String, FunctionIfc>[] funcArr = new HashMap.SimpleEntry[allFunctions.size()];

        AtomicInteger i = new AtomicInteger(0);
        allFunctions.forEach(f -> {
            funcArr[i.getAndAdd(1)] = new AbstractMap.SimpleEntry<>(f.getKey(), f.getValue());
        });

        return funcArr;
    }

    private Optional<String> getTagValue() {
        Optional<String> tagVal = getItem().getTags().stream()
                .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
                .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }

    private boolean isUd(String name) {
        return UD_FUNCTIONS.containsKey(name);
    }
}
