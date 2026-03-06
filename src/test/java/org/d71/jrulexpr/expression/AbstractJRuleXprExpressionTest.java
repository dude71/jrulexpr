package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.d71.jrulexpr.expression.eval.JrxEvaluationValueConverter;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.automation.jrule.rules.value.JRuleValue;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;

@ExtendWith(MockitoExtension.class)
class AbstractJRuleXprExpressionTest {

    @Mock
    private JrxItem mockItem;
    @Mock
    private Expression mockEvalExpression;
    @Mock
    private EvaluationValue mockEvalValue;
    @Mock
    private ASTNode mockAstNode;
    @Mock
    private Token mockToken;
    @Mock
    private JrxFunction mockFunction;
    @Mock
    private JrxItemRegistry mockItemRegistry;
    @Mock
    private JrxFunctionRegistry mockFunctionRegistry;
    @Mock
    private JrxEvaluationValueConverter mockValueConverter;

    private TestableAbstractJRuleXprExpression expression;

    @BeforeEach
    void setUp() throws Exception {
        expression = new TestableAbstractJRuleXprExpression(mockItem);
    }

    @Test
    void testConstructorStoresItem() {
        assertEquals(mockItem, expression.getContainerItem());
    }

    @Test
    void testEvaluateWithDefinitionCallsEvaluatedValue() throws Exception {
        when(mockEvalValue.getValue()).thenReturn("result");
        when(mockItem.getJrx()).thenReturn(Optional.of("result"));
        when(mockEvalExpression.evaluate()).thenReturn(mockEvalValue);

        Object result = expression.evaluate();

        assertEquals("converted_result", result);
    }

    @Test
    void testEvaluateWithoutDefinitionCallsDefaultValue() {
        Object result = expression.evaluate();

        assertEquals("default", result);
    }

    @Test
    void testEvaluateExpressionSuccess() throws Exception {
        when(mockEvalExpression.evaluate()).thenReturn(mockEvalValue);
        when(mockEvalValue.getValue()).thenReturn(42);

        Object result = expression.evaluateExpression();

        assertEquals(42, result);
        verify(mockEvalExpression).withValues(any());
        verify(mockEvalExpression).evaluate();
    }

    @Test
    void testEvaluateExpressionThrowsRuntimeExceptionOnError() throws Exception {
        when(mockEvalExpression.evaluate()).thenThrow(new RuntimeException("Test error"));

        assertThrows(RuntimeException.class, () -> expression.evaluateExpression());
    }

    @Test
    void testGetReferencedItemsExtractsItemsFromTokens() throws ParseException {
        when(mockItem.getJrx()).thenReturn(Optional.of("item1"));

        Token varToken = mock(Token.class);
        when(varToken.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(varToken.getValue()).thenReturn("item1");

        when(mockAstNode.getToken()).thenReturn(varToken);
        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(mockAstNode));

        when(mockItemRegistry.findItem("item1")).thenReturn(mockItem);

        Set<JrxItem> result = expression.getReferencedItems();

        assertEquals(1, result.size());
        assertTrue(result.contains(mockItem));
    }

    @Test
    void testGetReferencedItemsFiltersNonVariableTokens() throws ParseException {
        when(mockItem.getJrx()).thenReturn(Optional.of("FUNCT()"));

        Token funcToken = mock(Token.class);
        when(funcToken.getType()).thenReturn(TokenType.FUNCTION);

        when(mockAstNode.getToken()).thenReturn(funcToken);
        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(mockAstNode));

        Set<JrxItem> result = expression.getReferencedItems();

        assertEquals(0, result.size());
        verify(mockItemRegistry, never()).findItem(any());
    }

    @Test
    void testGetReferencedFunctionsExtractsFunctionsFromTokens() throws ParseException {
        when(mockItem.getJrx()).thenReturn(Optional.of("FUNC()"));

        Token funcToken = mock(Token.class);
        when(funcToken.getType()).thenReturn(TokenType.FUNCTION);
        when(funcToken.getValue()).thenReturn("FUNC");

        when(mockAstNode.getToken()).thenReturn(funcToken);
        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(mockAstNode));
        when(mockFunctionRegistry.isRegistered("FUNC")).thenReturn(true);
        when(mockFunctionRegistry.getFunctionInstance(funcToken.getValue())).thenReturn(mockFunction);

        Set<JrxFunction<?>> result = expression.getReferencedFunctions();

        assertEquals(1, result.size());
        assertTrue(result.contains(mockFunction));
    }

    @Test
    void testValueConversionExceptionMessage() {
        when(mockItem.getName()).thenReturn("testItem");

        IllegalStateException ex = expression.valueConversionException(123);

        assertTrue(ex.getMessage().contains("testItem"));
        assertTrue(ex.getMessage().contains("123"));
        assertTrue(ex.getMessage().contains("cannot be converted"));
    }

    @Test
    void testGetReferencedItemsFiltersVarTokens() throws ParseException {
        when(mockItem.getJrx()).thenReturn(Optional.of("jrxv_FOO && A=1"));

        Token varToken = mock(Token.class);
        when(varToken.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(varToken.getValue()).thenReturn("jrxv_FOO");
        when(mockAstNode.getToken()).thenReturn(varToken);

        ASTNode mockAstNode2 = Mockito.mock(ASTNode.class);
        Token varToken2 = mock(Token.class);
        when(varToken2.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(varToken2.getValue()).thenReturn("A");
        when(mockAstNode2.getToken()).thenReturn(varToken2);

        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(mockAstNode, mockAstNode2));
        when(mockItemRegistry.findItem("A")).thenReturn(mock(JrxItem.class));

        Set<JrxItem> result = expression.getReferencedItems();

        assertEquals(1, result.size());
        verify(mockItemRegistry, times(1)).findItem(any());
    }

    @Test
    void testEvaluateExpressionInvokesVarExpressionEvaluationAndPassesVarToEval() throws Exception {
        when(mockItem.getJrx()).thenReturn(Optional.of("jrxv-FOO"));

        Token varToken = mock(Token.class);
        when(varToken.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(varToken.getValue()).thenReturn("jrxv_FOO");
        when(mockAstNode.getToken()).thenReturn(varToken);
        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(mockAstNode));

        // prepare var expression mock
        JrxvExpression mockVarExpr = mock(JrxvExpression.class);
        when(mockVarExpr.evaluate()).thenReturn(7);

        when(mockEvalExpression.evaluate()).thenReturn(mockEvalValue);
        when(mockEvalValue.getValue()).thenReturn(0);

        try (MockedStatic<ExpressionFactory> mocked = mockStatic(ExpressionFactory.class)) {
            mocked.when(() -> ExpressionFactory.createJrxvExpression(mockItem, "jrxv_FOO")).thenReturn(mockVarExpr);

            Object result = expression.evaluateExpression();

            // evaluation proceeds (we stubbed evaluate to return 0)
            assertEquals(0, result);
            verify(mockVarExpr).evaluate();
            verify(mockEvalExpression).withValues(Mockito.argThat(m -> {
                if (!(m instanceof Map))
                    return false;
                Object v = ((Map) m).get("jrxv_FOO");
                return Integer.valueOf(7).equals(v);
            }));
        }
    }

    @Test
    void testEvaluateExpressionPassesItemAndVarValuesToEval() throws Exception {
        when(mockItem.getJrx()).thenReturn(Optional.of("A && jrxv-FOO"));

        // token for item A
        ASTNode itemNode = mock(ASTNode.class);
        Token itemToken = mock(Token.class);
        when(itemToken.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(itemToken.getValue()).thenReturn("A");
        when(itemNode.getToken()).thenReturn(itemToken);

        // token for var
        ASTNode varNode = mock(ASTNode.class);
        Token varToken = mock(Token.class);
        when(varToken.getType()).thenReturn(TokenType.VARIABLE_OR_CONSTANT);
        when(varToken.getValue()).thenReturn("jrxv_FOO");
        when(varNode.getToken()).thenReturn(varToken);

        when(mockEvalExpression.getAllASTNodes()).thenReturn(List.of(itemNode, varNode));

        // prepare referenced item A
        JrxItem mockA = mock(JrxItem.class);
        JRuleValue jRuleValue = mock(JRuleValue.class);
        when(mockA.getName()).thenReturn("A");
        when(mockA.getState()).thenReturn(jRuleValue);
        when(mockItemRegistry.findItem("A")).thenReturn(mockA);

        // prepare var expression
        JrxvExpression mockVarExpr = mock(JrxvExpression.class);
        when(mockVarExpr.evaluate()).thenReturn(13);

        when(mockEvalExpression.evaluate()).thenReturn(mockEvalValue);
        when(mockEvalValue.getValue()).thenReturn("ok");

        try (MockedStatic<ExpressionFactory> mocked = mockStatic(ExpressionFactory.class)) {
            mocked.when(() -> ExpressionFactory.createJrxvExpression(mockItem, "jrxv_FOO")).thenReturn(mockVarExpr);

            Object result = expression.evaluateExpression();

            assertEquals("ok", result);
            verify(mockVarExpr).evaluate();
            verify(mockEvalExpression).withValues(Mockito.argThat(m -> {
                if (!(m instanceof java.util.Map))
                    return false;
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) m;
                return map.get("A") == jRuleValue && Integer.valueOf(13).equals(map.get("jrxv_FOO"));
            }));
        }
    }

    // Concrete implementation for testing abstract class
    private class TestableAbstractJRuleXprExpression extends AbstractJRuleXprExpression<Object> {
        TestableAbstractJRuleXprExpression(JrxItem item) {
            super(item);
        }

        @Override
        protected Object convertEvaluatedValue(Object valueObj) {
            return "converted_" + valueObj;
        }

        @Override
        protected Object defaultValue() {
            return "default";
        }

        @Override
        public JRuleXprExpressionType getJrxType() {
            return JRuleXprExpressionType.JRX;
        }

        @Override
        Expression getEvalExpressionInstance() {
            return mockEvalExpression;
        }

        @Override
        protected JrxItemRegistry getItemRegistry() {
            return mockItemRegistry;
        }

        @Override
        protected JrxFunctionRegistry getFunctionRegistry() {
            return mockFunctionRegistry;
        }

        @Override
        protected JrxEvaluationValueConverter getValueConverter() {
            return mockValueConverter;
        }
    }
}