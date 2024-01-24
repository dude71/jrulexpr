package org.d71.jrulexpr.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.types.State;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.parser.Token.TokenType;

public class JrxExpression {
    private final static ItemTypeValueConverter converter = ItemTypeValueConverter.getInstance();

    private final ItemRegistry itemRegistry;

    private final JrxFunctionRegistry functionRegistry;

    private final String xpr;

    private Expression expression;

    public JrxExpression(String xpr) {
        this(xpr, JRuleEventHandler.get().getItemRegistry(), JrxFunctionRegistry.getInstance());
    }

    public JrxExpression(String xpr, ItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry) {
        this.xpr = xpr;
        validateXpr(xpr);
        this.itemRegistry = itemRegistry;
        this.functionRegistry = functionRegistry;
    }

    public Set<Item> getItems() {
        return getUndefinedVars().stream()
                .map(itemRegistry::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<JrxFunction<?>> getFunctionInstances() {
        return xpr == null ? Collections.emptySet()
                : functionRegistry.getFunctionTokens().stream()
                        .filter(f -> xpr.contains(f))
                        .map(functionRegistry::getFunctionInstance)
                        .map(this::prepareFunctionInstance)
                        .collect(Collectors.toSet());
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
            Expression xpr = getExpression().get();
            Map<String, State> values = getItems().stream().collect(Collectors.toMap(Item::getName, Item::getState));
            xpr.withValues(values);
            try {
                return xpr.evaluate().getValue();
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

    @SuppressWarnings("unchecked")
    private Entry<String, FunctionIfc>[] getFunctionInstanceEntries() {
        return getFunctionInstances().stream()
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