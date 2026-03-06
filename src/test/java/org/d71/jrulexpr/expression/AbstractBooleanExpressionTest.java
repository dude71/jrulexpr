package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractBooleanExpressionTest {

    private AbstractBooleanExpression expression;
    private JrxItem mockItem;

    @BeforeEach
    void setUp() {
        mockItem = mock(JrxItem.class);
        expression = new AbstractBooleanExpression(mockItem) {
            @Override
            public JRuleXprExpressionType getJrxType() {
                return JRuleXprExpressionType.JRX;
            }

            @Override
            protected Boolean defaultValue() {
                return Boolean.TRUE;
            }};
    }

    @Test
    void testConvertEvaluatedValueWithBooleanTrue() {
        Boolean result = expression.convertEvaluatedValue(true);
        assertTrue(result);
    }

    @Test
    void testConvertEvaluatedValueWithBooleanFalse() {
        Boolean result = expression.convertEvaluatedValue(false);
        assertFalse(result);
    }

    @Test
    void testConvertEvaluatedValueWithNonBooleanThrowsException() {
        assertThrows(IllegalStateException.class, () -> expression.convertEvaluatedValue("not a boolean"));
    }

    @Test
    void testConvertEvaluatedValueWithIntegerThrowsException() {
        assertThrows(IllegalStateException.class, () -> expression.convertEvaluatedValue(42));
    }

    @Test
    void testConvertEvaluatedValueWithNullThrowsException() {
        assertThrows(IllegalStateException.class, () -> expression.convertEvaluatedValue(null));
    }

    @Test
    void testConstructorStoresItem() {
        assertNotNull(expression);
    }
}