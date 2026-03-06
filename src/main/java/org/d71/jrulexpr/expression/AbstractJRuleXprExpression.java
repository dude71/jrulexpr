package org.d71.jrulexpr.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.d71.jrulexpr.expression.eval.EvalExExpressionFactory;
import org.d71.jrulexpr.expression.eval.JrxEvaluationValueConverter;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;

public abstract class AbstractJRuleXprExpression<T> implements JRuleXprExpression<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJRuleXprExpression.class);

    private static final String REGEX_JRXV = "^jrxv?_\\S+";
    
    private final JrxItem item;
    private Expression expression;

    public AbstractJRuleXprExpression(JrxItem item) {
        this.item = item;
    }

    public JrxItem getContainerItem() {
        return item;
    }

    @Override
    public Optional<String> getDefinition() {
        Optional<String> definition = JRuleXprExpression.super.getDefinition();
        return definition.isPresent() ? Optional.of(JrxItem.sanitizeJrxvs(definition.get())) : definition;
    }
   
    @Override
    public final T evaluate() {
        T value;
        if (getDefinition().isPresent()) {
            value = evaluatedValue();
        } else {
            value = defaultValue();
        }
        return value;
    }
    
    protected T evaluatedValue() {
        Object valueObj = evaluateExpression();
        LOGGER.debug("{}.{} evaluates to raw value {} ({})", new Object[] { getContainerItem().getName(), getJrxType().getToken(), valueObj, valueObj == null ? "null" : valueObj.getClass().getSimpleName() });
        return convertEvaluatedValue(valueObj);
    }

    protected Object evaluateExpression() {
        Expression expr = getEvalExpressionInstance();
        expr.withValues(getAllValues());
        try {
            EvaluationValue evalValue = expr.evaluate();
            return evalValue.getValue(); // value can be null
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected JrxItemRegistry getItemRegistry() {
        return JrxItemRegistry.getInstance();
    }

    protected JrxFunctionRegistry getFunctionRegistry() {
        return JrxFunctionRegistry.getInstance();
    }

    protected JrxEvaluationValueConverter getValueConverter() {
        return JrxEvaluationValueConverter.getInstance();
    }
    
    Expression getEvalExpressionInstance() {
        if (expression == null) {
            expression = EvalExExpressionFactory.createExpression(this, getItemRegistry(), getFunctionRegistry(), getValueConverter());
        }
        return expression;
    }

    private Map<String, Object> getAllValues() {
        Map<String, Object> allValues = new HashMap<>();
        allValues.putAll(getItemValues());
        allValues.putAll(getVarValues());
        return allValues;
    }

    private Map<String, JRuleValue> getItemValues() {
        // OpenJDK bug; null values in Map cause NullPointerException!
        //Map<String, JRuleValue> values = getItems().stream().collect(Collectors.toMap(JrxItem::getName, JrxItem::getState));
        Map<String, JRuleValue> values = getReferencedItems().stream().collect(HashMap::new, (m,v)->m.put(v.getName(), v.getState()), HashMap::putAll);
        return values;
    }

    private Map<String, Object> getVarValues(){
        Map<String, Object> varValues = new HashMap<>();
        JrxItem item = getContainerItem();
        for (String varName : getReferencedVars()) {
            LOGGER.debug(varName + " is referenced in " + getJrxType().getToken() + ": '" + getDefinition().orElse("<undef>") + "' of item " + item.getName());
            JrxvExpression varExpression = ExpressionFactory.createJrxvExpression(item, varName);
            varValues.put(varName, varExpression.evaluate());
        }
        return varValues;
    }

    @Override
    public Set<JrxItem> getReferencedItems(boolean includeJrxvItems) {
        Set<JrxItem> items;
        if (getDefinition().isPresent()) {
            Set<JrxItem> itemsFromExpression = getReferencedItemsFromExpression();
            if (includeJrxvItems) {
                items = new HashSet<>();
                items.addAll(itemsFromExpression);
                items.addAll(getReferencedItemsFromVarsInExpression());
            } else {
                items = itemsFromExpression;
            }
            return items;
        } else {
            items = Collections.emptySet();
        }
        return items;
    }

    private Set<JrxItem> getReferencedItemsFromExpression() {
        Expression expr = getEvalExpressionInstance();
        try {
            return expr.getAllASTNodes().stream()
                .map(ASTNode::getToken)
                .filter(t -> t.getType() == TokenType.VARIABLE_OR_CONSTANT && !t.getValue().matches(REGEX_JRXV))
                .map(Token::getValue)
                .map(getItemRegistry()::findItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<JrxFunction<?>> getReferencedFunctions(boolean includeJrxvFunctions) {
        Set<JrxFunction<?>> functions;
        if (getDefinition().isPresent()) {
            Set<JrxFunction<?>> functionsFromExpression = getReferencedFunctionsFromExpression();
            if (includeJrxvFunctions) {
                functions = new HashSet<>();
                functions.addAll(functionsFromExpression);
                functions.addAll(getReferencedFunctionsFromVarsInExpression());
            } else {
                functions = functionsFromExpression;
            }
        } else {
            return Collections.emptySet();
        }
        return functions;
    }

    private Set<JrxFunction<?>> getReferencedFunctionsFromExpression() {
        Expression expr = getEvalExpressionInstance();
        try {
            Set<JrxFunction<?>> functions = new HashSet<>();
            for (ASTNode n : expr.getAllASTNodes()) {
                if (n.getToken().getType() == TokenType.FUNCTION) {
                    String t = n.getToken().getValue();
                    if (getFunctionRegistry().isRegistered(t)) {
                        LOGGER.debug("Function '{}' is referenced in {}: '{}' of item {}", new Object[] { t, getJrxType().getToken(), getDefinition().orElse("<undef>"), getContainerItem().getName() });
                        JrxFunction<?> f = getFunctionRegistry().getFunctionInstance(t);
                        f.setItem(getContainerItem());
                        f.setParameters(n.getParameters().stream()
                            .map(p -> p.getToken().getValue())
                            .collect(Collectors.toList())
                        );
                        functions.add(f);
                    }
                }
            }
            return functions;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<JrxItem> getReferencedItemsFromVarsInExpression() {
        Set<JrxItem> items = new HashSet<>();
        JrxItem item = getContainerItem();
        for (String varName : getReferencedVars()) {
            JrxvExpression varExpression = ExpressionFactory.createJrxvExpression(item, varName);
            items.addAll(varExpression.getReferencedItems(true));
        }
        return items;
    }
    
    private Set<JrxFunction<?>> getReferencedFunctionsFromVarsInExpression() {
        Set<JrxFunction<?>> functions = new HashSet<>();
        JrxItem item = getContainerItem();
        for (String varName : getReferencedVars()) {
            JrxvExpression varExpression = ExpressionFactory.createJrxvExpression(item, varName);
            functions.addAll(varExpression.getReferencedFunctions(true));
        }
        return functions;
    }

    private Set<String> getReferencedVars() {
        return getDefinition().isEmpty() ? Collections.emptySet() : getReferencedVarsFromExpression();
    }

    private Set<String> getReferencedVarsFromExpression() {
        Expression expr = getEvalExpressionInstance();
        try {
            return expr.getAllASTNodes().stream()
                .map(ASTNode::getToken)            
                .filter(t -> t.getType() == TokenType.VARIABLE_OR_CONSTANT && t.getValue().matches(REGEX_JRXV))
                .map(Token::getValue)
                .collect(Collectors.toSet());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected IllegalStateException valueConversionException(Object valueObj) {
        return new IllegalStateException(
            getContainerItem().getName() + "." + 
            getJrxType().getToken() + ": " +
            getDefinition().orElse("<undef>") + 
            " evaluates to " + valueObj + 
            " which cannot be converted to expected type!");
    }

    protected abstract T convertEvaluatedValue(Object valueObj);

    protected abstract T defaultValue();    
}
