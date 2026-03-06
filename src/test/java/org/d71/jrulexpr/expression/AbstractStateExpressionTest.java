package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.expression.eval.JRuleValueHandler;
import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.library.CoreItemFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractStateExpressionTest {

    static class TestStateExpression extends AbstractStateExpression {
        public TestStateExpression(JrxItem item) { 
            super(item); 
        }

        @Override
        public JRuleXprExpressionType getJrxType() {
            return JRuleXprExpressionType.JRXT;    
        }

        @Override
        protected JRuleValue defaultValue() {
            return JRuleValueHandler.convertObjectToJRuleValue(1, CoreItemFactory.NUMBER);
        }
    }

    @Test
    void convertEvaluatedValue_returnsSameWhenJRuleValue() {
        JrxItem item = mock(JrxItem.class);
        TestStateExpression expr = new TestStateExpression(item);
        JRuleValue value = mock(JRuleValue.class);

        try (MockedStatic<JRuleValueHandler> mocked = mockStatic(JRuleValueHandler.class)) {
            JRuleValue result = expr.convertEvaluatedValue(value);
            assertSame(value, result);
            mocked.verifyNoInteractions();
        }
    }

    @Test
    void convertEvaluatedValue_usesValueConverterWhenNotJRuleValue() {
        JrxItem item = mock(JrxItem.class);
        when(item.getType()).thenReturn("MyType");
        TestStateExpression expr = new TestStateExpression(item);

        Object raw = "rawValue";
        JRuleValue converted = mock(JRuleValue.class);

        try (MockedStatic<JRuleValueHandler> mocked = mockStatic(JRuleValueHandler.class)) {
            mocked.when(() -> JRuleValueHandler.convertObjectToJRuleValue(raw, "MyType")).thenReturn(converted);

            JRuleValue result = expr.convertEvaluatedValue(raw);
            assertSame(converted, result);

            mocked.verify(() -> JRuleValueHandler.convertObjectToJRuleValue(raw, "MyType"), times(1));
        }
    }
}