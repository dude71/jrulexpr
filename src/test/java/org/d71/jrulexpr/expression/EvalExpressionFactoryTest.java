package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.Set;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.functions.FunctionIfc;

import org.d71.jrulexpr.expression.eval.EvalExExpressionFactory;
import org.d71.jrulexpr.expression.eval.JrxEvaluationValueConverter;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EvalExpressionFactoryTest {
    private JRuleXprExpression<?> expr;
    private JrxItemRegistry itemRegistry;
    private JrxFunctionRegistry functionRegistry;
    private JrxEvaluationValueConverter converter;
    private JrxItem item;

    @BeforeEach
    void setup() {
        expr = mock(JRuleXprExpression.class);
        itemRegistry = mock(JrxItemRegistry.class);
        functionRegistry = mock(JrxFunctionRegistry.class);
        converter = mock(JrxEvaluationValueConverter.class);
        item = mock(JrxItem.class);
        when(expr.getContainerItem()).thenReturn(item);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void createExpression_registersReferencedFunctionsAndConfiguresThem() {
        when(expr.getDefinition()).thenReturn(Optional.of("foo(1) + 2"));
        when(functionRegistry.getFunctionTokens()).thenReturn(Set.of("foo", "bar"));

        // Create a JrxFunction mock that is also a FunctionIfc
        JrxFunction fooFunc = (JrxFunction) mock(JrxFunction.class, withSettings().extraInterfaces(FunctionIfc.class));
        when(fooFunc.getToken()).thenReturn("foo");
        when(functionRegistry.getFunctionInstance("foo")).thenReturn(fooFunc);

        // Execute SUT
        Expression expression = EvalExExpressionFactory.createExpression(expr, itemRegistry, functionRegistry, converter);

        // Assertions / verifications
        assertNotNull(expression);
        verify(functionRegistry).getFunctionTokens();
        verify(functionRegistry).getFunctionInstance("foo");
        verify(functionRegistry, never()).getFunctionInstance("bar");
        verify(fooFunc).setItem(item);
        verify(fooFunc).setItemRegistry(itemRegistry);
    }

    @Test
    void createExpression_registersReferencedFunctionNotFound() {
        when(expr.getDefinition()).thenReturn(Optional.of("bar(1) + foobar(2) + bar()"));
        when(functionRegistry.getFunctionTokens()).thenReturn(Set.of("foo", "bar"));
        JrxFunction fooFunc = (JrxFunction) mock(JrxFunction.class, withSettings().extraInterfaces(FunctionIfc.class));
        when(fooFunc.getToken()).thenReturn("foo");
        JrxFunction barFunc = (JrxFunction) mock(JrxFunction.class, withSettings().extraInterfaces(FunctionIfc.class));
        when(barFunc.getToken()).thenReturn("bar");

        // Execute SUT
        Expression expression = EvalExExpressionFactory.createExpression(expr, itemRegistry, functionRegistry, converter);

        assertNotNull(expression);
        verify(functionRegistry).getFunctionTokens();
        verify(functionRegistry, never()).getFunctionInstance("foobar");
        verify(functionRegistry, times(1)).getFunctionInstance("bar");
    }    

    @Test
    void createExpression_throwsWhenDefinitionMissing() {
        @SuppressWarnings("unchecked")
        JRuleXprExpression<Object> expr = mock(JRuleXprExpression.class);
        when(expr.getDefinition()).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                EvalExExpressionFactory.createExpression(expr, itemRegistry, functionRegistry, converter));
    }
}