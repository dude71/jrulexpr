package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.CoreItemFactory;

public class JrxItemExpressionTest extends AbstractJrxExpressionTest {
    @Test
    public void evalTwoConditionsAnd() {
        createMockedItem("ITM1", CoreItemFactory.NUMBER, "1");
        createMockedItem("ITM2", CoreItemFactory.NUMBER, "1");
        JrxItem item = createMockedItem("JRXITM",
                CoreItemFactory.DIMMER,
                "1",
                "jrx=ITM1 == 1 && ITM2 == 1");

        // SUT
        JrxItemExpression jrxItemExpression = createJrxItemExpression(item);
        Boolean eval = (Boolean) jrxItemExpression.evaluate();

        assertTrue(eval);
    }

    @Test
    public void evalWithFunction() {
        JrxItem item = createMockedItem("JRXITM",
                CoreItemFactory.DIMMER,
                "1",
                "jrx=nItem1 == 10 && HOUR() < 25", "jrxt=2");

        assertTrue(item.getFunctions().size() == 1);
        assertTrue(item.getFunctions().iterator().next().getRuleTrigger().get().getCronExpression() != null);
        assertNotEquals(item.getState(), item.evaluateNewValue().get());
    }

    @Test
    public void evalJrxp() {
        JrxItem item = createMockedItem("ITM", CoreItemFactory.NUMBER, "1", "jrxp=HOUR() < 24", "jrx=true");
        JrxpItemExpression jrxp = new JrxpItemExpression(item, itemRegistry, functionRegistry);

        assertTrue((Boolean)jrxp.evaluate());
    }

    private JrxItemExpression createJrxItemExpression(JrxItem item) {
        return new JrxItemExpression(item, itemRegistry, functionRegistry) ;
    }
}
