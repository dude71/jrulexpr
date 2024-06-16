package org.d71.jrulexpr.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.parser.Token.TokenType;

public class JrxExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxExpression.class);
    private static final ItemTypeValueConverter converter = ItemTypeValueConverter.getInstance();

    final JrxItemRegistry itemRegistry;

    final JrxFunctionRegistry functionRegistry;

    private final String xpr;

    private Expression expression;

    public JrxExpression(String xpr) {
        this(xpr, JrxItemRegistry.getInstance(), JrxFunctionRegistry.getInstance());
    }

    public JrxExpression(String xpr, JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        this.xpr = xpr;
        validateXpr(xpr);
        this.itemRegistry = itemRegistry;
        this.functionRegistry = functionRegistry;
    }

    public Set<JrxItem> getItems() {
        return getUndefinedVars().stream()
                .map(itemRegistry::getItem)
                .collect(Collectors.toSet());
    }

    public List<JrxFunction<?>> getFunctionInstances() {
        Optional<Expression> optXpr = getExpression();
        try {
            return optXpr.isPresent() ? optXpr.get().getAllASTNodes().stream()
                .filter(n -> n.getToken().getType() == TokenType.FUNCTION)
                .filter(n -> functionRegistry.isRegistered(n.getToken().getValue()))
                .map(n -> { 
                    JrxFunction<?> f = functionRegistry.getFunctionInstance(n.getToken().getValue());
                    f.setParameters(n.getParameters().stream()
                        .map(p -> p.getToken().getValue())
                        .collect(Collectors.toList())
                    );
                    prepareFunctionInstance(f);
                    return f;
                })
                .collect(Collectors.toList()) : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getFunctionTokens() {
        Optional<Expression> optXpr = getExpression();
        try {
            return optXpr.isPresent() ? optXpr.get().getAllASTNodes().stream()
                    .map(n -> n.getToken())
                    .filter(t -> t.getType() == TokenType.FUNCTION && functionRegistry.isRegistered(t.getValue()))
                    .map(t -> t.getValue())
                    .collect(Collectors.toSet()) : Collections.emptySet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object evaluate() {
        Optional<Expression> optXpr = getExpression();
        if (optXpr.isPresent()) {
            Expression xpr = optXpr.get();
            // OpenJDK bug; null values in Map cause NullPointerException!
            //Map<String, JRuleValue> values = getItems().stream().collect(Collectors.toMap(JrxItem::getName, JrxItem::getState));
            Map<String, JRuleValue> values = getItems().stream().collect(HashMap::new, (m,v)->m.put(v.getName(), v.getState()), HashMap::putAll);
            xpr.withValues(values);
            try {
                LOGGER.debug("evaluating xpr={}", xpr);
                EvaluationValue evaluate = xpr.evaluate();
                LOGGER.debug("evaluated xpr={}", xpr);
                LOGGER.debug("xpr {} => {}", new Object[] { xpr.getExpressionString(), evaluate.getValue() });
                if (LOGGER.isDebugEnabled() && evaluate.isBooleanValue() && !evaluate.getBooleanValue()) {
                    values.forEach((k, v) -> { 
                        LOGGER.debug("{} = {}", new Object[] {k, v}); 
                    });
                }
                return evaluate.getValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return getDefaultValue();
        }
    }

    public String getXpr() {
        return xpr;
    }

    protected void validateXpr(String xpr) {
    }

    protected Object getDefaultValue() {
        return null;
    }

    protected JrxFunction<? extends Object> prepareFunctionInstance(JrxFunction<? extends Object> f) {
        return f;
    }


    private Set<JrxFunction<?>> getUniqFunctionInstances() {
        return xpr == null ? Collections.emptySet()
                : functionRegistry.getFunctionTokens().stream()
                .filter(f -> xpr.contains(f))
                .map(functionRegistry::getFunctionInstance)
                .map(this::prepareFunctionInstance)
                .collect(Collectors.toSet());
    }
    
    @SuppressWarnings("unchecked")
    private Entry<String, FunctionIfc>[] getFunctionInstanceEntries() {
        return getUniqFunctionInstances().stream()
                .filter(f -> f instanceof FunctionIfc)
                .map(f -> Map.entry(f.getToken(), FunctionIfc.class.cast(f)))
                .toArray(size -> new Map.Entry[size]);
    }

    private Optional<Expression> getExpression() {
        if (expression == null && xpr != null) {
            expression = new Expression(xpr,
                    ExpressionConfiguration.builder()
                            .evaluationValueConverter(converter)
                            .build()
                            .withAdditionalFunctions(getFunctionInstanceEntries()));
        }
        return Optional.ofNullable(expression);
    }

    private Set<String> getUndefinedVars() {
        try {
            Optional<Expression> optXpr = getExpression();
            return optXpr.isPresent() ? optXpr.get().getUndefinedVariables() : Collections.emptySet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}