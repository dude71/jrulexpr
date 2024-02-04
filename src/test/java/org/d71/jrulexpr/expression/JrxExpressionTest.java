package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.d71.jrulexpr.function.HourFunction;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.CoreItemFactory;

public class JrxExpressionTest extends AbstractJrxExpressionTest {
    // SUT
    private JrxExpression jrxExpression;

    @Test
    public void getItemsNone() {
        // SUT
        jrxExpression = new JrxExpression("1 > 2", itemRegistry, functionRegistry);

        assertEquals(0, jrxExpression.getItems().size());
    }

    @Test
    public void getItemsSome() {
        // SUT
        jrxExpression = new JrxExpression("nItem1 < nItem2", itemRegistry, functionRegistry);
        assertEquals(2, jrxExpression.getItems().size());
    }

    @Test
    public void getFunctionInstancesNone() {
        // SUT
        jrxExpression = new JrxExpression("1 == 1", itemRegistry, functionRegistry);

        assertEquals(0, jrxExpression.getFunctionInstances().size());
    }

    @Test
    public void getFunctionInstancesSome() {
        // SUT
        jrxExpression = new JrxExpression("item1 + HOUR() + HOUR() && HOST(\"localhost\")", itemRegistry, functionRegistry);

        assertEquals(3, jrxExpression.getFunctionInstances().size());
    }

    @Test
    public void getFunctionTokens() {
        jrxExpression = new JrxExpression("HOUR() == 1 || HOUR() == 2 && HOST(\"tst\")", itemRegistry, functionRegistry);

        assertArrayEquals(new String[] { "HOUR", "HOST" }, jrxExpression.getFunctionTokens().toArray(new String[2]));
    }

    @Test
    public void evaluateTrue() {
        jrxExpression = createJrxExpression("HOUR() < 25 && nItem1 == 10");

        assertTrue((Boolean)jrxExpression.evaluate());
    }

    @Test
    public void evaluateNumber() {
        jrxExpression = createJrxExpression("nItem1 + nItem2");

        assertEquals(30, ((BigDecimal)jrxExpression.evaluate()).intValue());
    }

    @Test
    public void evaluateNumberFunctions() {
        jrxExpression = createJrxExpression("HOUR() + HOUR()");

        assertEquals((new HourFunction()).getValue().intValue(), ((BigDecimal)jrxExpression.evaluate()).intValue() / 2);
    }

    @Test
    public void evaluateNullValue() {
        createMockedItem("itm", CoreItemFactory.NUMBER, null);
        jrxExpression = createJrxExpression("itm == 1");

        assertFalse((Boolean)jrxExpression.evaluate());
    }
}
