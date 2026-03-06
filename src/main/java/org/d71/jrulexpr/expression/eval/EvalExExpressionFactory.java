package org.d71.jrulexpr.expression.eval;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.d71.jrulexpr.expression.JRuleXprExpression;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.functions.FunctionIfc;

/**
 * Factory class responsible for creating Expression instances from JRuleXprExpression definitions, extracting the necessary functions and items to configure the Expression properly for evaluation.
 * 
 * @author d71
 */
public class EvalExExpressionFactory {
    /**
     * Factory method to create an Expression instance from a JRuleXprExpression, extracting the expression definition, associated item, and used functions to configure the Expression properly.
     * 
     * @param expr The JRuleXprExpression instance containing the expression definition and associated item
     * @return Configured Expression instance ready for evaluation
     */
    public static Expression createExpression(JRuleXprExpression<?> expr, JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry, JrxEvaluationValueConverter converter) {
        String definition = expr.getDefinition()
                .orElseThrow(() -> new IllegalArgumentException("Expression definition is required"));
        JrxItem item = expr.getContainerItem();

        ExpressionConfiguration configuration = ExpressionConfiguration.builder()
                .evaluationValueConverter(converter)
                .build()
                .withAdditionalFunctions(getEvalFunctionEntries(itemRegistry, functionRegistry, item, definition));

        return new Expression(definition, configuration);
    }

    /**
     * Utility method to extract the functions from an expression definition and prepare them for registration in the ExpressionConfiguration.
     * 
     * @param definition The expression definition string
     * @param item The JrxItem associated with the expression, to be set in the function instances
     * @return Array of Map.Entry where the key is the function token and the value is the FunctionIfc instance, ready for registration in ExpressionConfiguration
     */
    @SuppressWarnings("unchecked")
    private static Entry<String, FunctionIfc>[] getEvalFunctionEntries(JrxItemRegistry itemRegistry, JrxFunctionRegistry functionRegistry, JrxItem item, String definition) {
        return getReferencedFunctions(functionRegistry, definition).stream()
                .filter(f -> f instanceof FunctionIfc)
                .map(f -> {
                    f.setItem(item);
                    f.setItemRegistry(itemRegistry);
                    return f;
                })
                .map(f -> Map.entry(f.getToken(), FunctionIfc.class.cast(f)))
                .toArray(size -> new Map.Entry[size]);
    }

    /**
     * Utility method to extract the functions from an expression definition.
     * 
     * @param definition The expression definition string
     * @return Set of JrxFunction instances referenced in the expression
     */
    private static Set<JrxFunction<?>> getReferencedFunctions(JrxFunctionRegistry functionRegistry, String definition) {
        return functionRegistry.getFunctionTokens().stream()
            .filter(ft -> {
                Pattern pattern = Pattern.compile("\\b(" + Pattern.quote(ft) + ")\\s*\\(");
                return pattern.matcher(definition).find();
            })
            .map(functionRegistry::getFunctionInstance)
            .collect(Collectors.toSet());
    }

}
