package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JrxcExpression Tests")
class JrxcExpressionTest {

    @Mock
    private JrxItem mockItem;

    private JrxcExpression expression;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        expression = new JrxcExpression(mockItem);
    }

    @Test
    @DisplayName("should return JRXC type")
    void testGetJrxType() {
        assertEquals(JRuleXprExpressionType.JRXC, expression.getJrxType());
    }

    @Test
    @DisplayName("should parse simple key-value pairs")
    void testEvaluateSimpleKeyValuePairs() {
        when(mockItem.getJrxc()).thenReturn(Optional.of("key1=value1, key2=value2"));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should handle quoted values with commas")
    void testEvaluateQuotedValuesWithCommas() {
        when(mockItem.getJrxc()).thenReturn(Optional.of("key1='value, with comma', key2=value2"));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals("value, with comma", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    @DisplayName("should trim whitespace around keys and values")
    void testEvaluateTrimsWhitespace() {
        when(mockItem.getJrxc()).thenReturn(Optional.of("  key1  =  value1  ,  key2  =  value2  "));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    @DisplayName("should handle name-only entries without equals sign")
    void testEvaluateNameOnly() {
        when(mockItem.getJrxc()).thenReturn(Optional.of("key1=value1,key2,key3=value3, key4"));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals("value1", result.get("key1"));
        assertEquals("", result.get("key2")); // key2 has no value, so it should be an empty string
        assertEquals("value3", result.get("key3"));
        assertEquals("", result.get("key4")); // key4 has no value, so it should be an empty string
        assertNull(result.get("key5"));
    }    

    @Test
    @DisplayName("should throw when definition is missing")
    void testEvaluateThrowsWhenDefinitionMissing() {
        when(mockItem.getJrxc()).thenReturn(Optional.empty());
        
        assertThrows(IllegalStateException.class, () -> expression.evaluateExpression());
    }

    @Test
    @DisplayName("should return empty Properties as default value")
    void testDefaultValue() {
        Properties result = expression.defaultValue();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should throw when converting non-List value")
    void testConvertEvaluatedValueThrowsForNonList() {
        assertThrows(Exception.class, () -> expression.convertEvaluatedValue("not a list"));
    }

    @Test
    @DisplayName("should handle empty input")
    void testEvaluateEmptyInput() {
        when(mockItem.getJrxc()).thenReturn(Optional.of(""));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should handle single key-value pair")
    void testEvaluateSingleKeyValuePair() {
        when(mockItem.getJrxc()).thenReturn(Optional.of("key=value"));
        
        Properties result = (Properties) expression.evaluateExpression();
        
        assertEquals("value", result.get("key"));
        assertEquals(1, result.size());
    }
}