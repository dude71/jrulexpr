package org.d71.jrulexpr.rule;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.d71.jrulexpr.rule.functions.HourFunction;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.Token.TokenType;

public class ItemExprEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemExprEvaluator.class);

    private ItemRegistry itemRegistry = null;

    private EvaluationValueConverterIfc valueConverter = new EvaluationValueConverterIfc() {
        private EvaluationValueConverterIfc defaultConverter = new DefaultEvaluationValueConverter();

        @Override
        public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
            EvaluationValue val = null;
            LOGGER.debug(object.toString());

            if (object instanceof DecimalType)
                val = EvaluationValue.numberValue(
                        ((DecimalType) object).toBigDecimal()
                );
            else if (object instanceof OnOffType)
                val = EvaluationValue.stringValue(
                        ((OnOffType) object).toString()
                );
            else
                val = defaultConverter.convertObject(object, configuration);

            return val;
        }
    };

    public ItemExprEvaluator(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public EvaluationValue eval(String itemName) throws Exception {
        Item item = itemRegistry.getItem(itemName);
        String jrx = JrxParser.getJrx(item).orElseThrow(() -> new IllegalStateException("jrx tag must be present!"));
        
        EvaluationValue ev = evalXpr(jrx);
        if (!ev.isBooleanValue()) {
            throw new IllegalStateException("jrx must evaluate to boolean!");
        }
        LOGGER.debug("jrx: " + jrx + " ev: " + ev);

        Optional<String> jrxv = ev.getBooleanValue() ? JrxParser.getJrxt(item) : JrxParser.getJrxf(item);

        return jrxv.isPresent() ? evalXpr(jrxv.get()) : getDefault(ev.getBooleanValue(), item);
    }

    protected Expression getExpression(String expression) {
        LOGGER.info("EvalEx..");
        return new Expression(expression,
                ExpressionConfiguration.builder()
                        .evaluationValueConverter(valueConverter)
                        .build()
                        .withAdditionalFunctions(Map.entry("HOUR", new HourFunction())));
    }

    protected Set<String> getUdFunctions(Expression expression) throws Exception {
        return new HashSet<>(expression.getAllASTNodes().stream()
                .map(n -> n.getToken())
                .filter(t -> t.getType() == TokenType.FUNCTION && isUd(t.getValue()))
                .map(t -> t.getValue())
                .toList());
    }

    private boolean isUd(String name) {
        return "HOUR".equals(name);
    }

    private EvaluationValue evalXpr(String expression) throws Exception {
        LOGGER.debug("eval: " + expression);

        Expression ezyExpr = getExpression(expression);

        ezyExpr.getUndefinedVariables().forEach(v -> LOGGER.trace("var: " + v));

        List<Item> items = ezyExpr.getUndefinedVariables().stream().map(v -> itemRegistry.get(v)).toList();

        items.forEach(i -> LOGGER.trace("itm: " + i.getName() + " " + i.getState() + " " + i.getType() + " " + i.getClass()));

        items.forEach(i -> ezyExpr.with(i.getName(), i.getState()));
        return ezyExpr.evaluate();
    }

    private EvaluationValue getDefault(boolean result, Item item) {
        EvaluationValue ev = EvaluationValue.nullValue();
        if (CoreItemFactory.DIMMER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(result ? 90 : 0));
        } else if (CoreItemFactory.NUMBER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(result ? 1 : 0));
        } else if (CoreItemFactory.SWITCH.equals(item.getType())) {
            ev = EvaluationValue.stringValue(result ? "ON" : "OFF");
        }

        return ev;
    }

}
