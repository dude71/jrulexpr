package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;

import static org.junit.jupiter.api.Assertions.*;

public class JrxItemExpressionTest extends AbstractJrxExpressionTest {
    @Test
    public void evalTwoConditionsAnd() {
        createMockedItem("ITM1", CoreItemFactory.NUMBER, "1");
        createMockedItem("ITM2", CoreItemFactory.NUMBER, "1");
        Item item = createMockedItem("JRXITM",
                CoreItemFactory.DIMMER,
                "1",
                "jrx=ITM1 == 1 && ITM2 == 1");

        // SUT
        JrxItemExpression jrxItemExpression = createJrxItemExpression(toJrxItem(item));
        Boolean eval = (Boolean) jrxItemExpression.evaluate();

        assertTrue(eval);
    }

    @Test
    public void evalWithFunction() {
        JrxItem item = createJrxItem("JRXITM",
                CoreItemFactory.DIMMER,
                "1",
                "jrx=nItem1 == 10 && HOUR() < 25", "jrxt=2");

        assertTrue(item.getFunctions().size() == 1);
        assertTrue(item.getFunctions().iterator().next().getCronExpression() != null);
        assertNotEquals(item.getState(), item.evaluateNewState().get());
    }

    private JrxItem createJrxItem(String name, String type, String value, String... tags) {
        return toJrxItem(createMockedItem(name, type, value, tags));
    }

    private JrxItemExpression createJrxItemExpression(JrxItem item) {
        return new JrxItemExpression(item, itemRegistry, functionRegistry) ;
    }

    private JrxItem toJrxItem(Item item) {
        return new JrxItem(item) {
            @Override
            protected JrxItemExpression createJrxItemExpression() { return new JrxItemExpression(this, itemRegistry, functionRegistry); }
            @Override
            protected JrxpItemExpression createJrxpItemExpression() { return new JrxpItemExpression(this, itemRegistry, functionRegistry); }
            @Override
            protected JrxtItemExpression createJrxtItemExpression() { return new JrxtItemExpression(this, itemRegistry, functionRegistry); }
            @Override
            protected JrxfItemExpression createJrxfItemExpression() { return new JrxfItemExpression(this, itemRegistry, functionRegistry); }
        };
    }
}
