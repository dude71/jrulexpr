package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;
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
        assertTrue(item.getFunctions().iterator().next().getRuleTriggers().iterator().next().getCronExpression() != null);
        assertNotEquals(item.getState(), item.evaluateNewState());
    }

    @Test
    public void evalJrxp() {
        JrxItem item = createMockedItem("ITM", CoreItemFactory.NUMBER, "1", "jrxp=HOUR() < 24", "jrx=true");
        JrxpItemExpression jrxp = new JrxpItemExpression(item, itemRegistry, functionRegistry);

        assertTrue((Boolean)jrxp.evaluate());
    }

    @Test
    public void evalJrxpWithNullValue() {
        createMockedItem("NITM", CoreItemFactory.STRING, null);
        JrxItem item = createMockedItem("ITM", CoreItemFactory.NUMBER, null, "jrxp=NITM == \"txt\"", "jrx=true");
        JrxpItemExpression jrxp = new JrxpItemExpression(item, itemRegistry, functionRegistry);

        assertFalse(jrxp.evaluateToBoolean());
    }

    @Test
    public void getTriggeringItems() {
        JrxItem itm1 = createMockedItem("itm1", CoreItemFactory.STRING, "test");
        JrxItem itm2 = createMockedItem("itm2", CoreItemFactory.NUMBER, "2", "jrxp=SUBSTR(itm1, 0, 1) == \"t\"");

        assertEquals(1, itm2.getTriggeringItems().size());
        assertEquals(itm1, itm2.getTriggeringItems().iterator().next());
    }

    @Test
    public void evalJrxWithVar() {
        createMockedItem("X", CoreItemFactory.NUMBER, "1");
        JrxItem itm = createMockedItem("Z", CoreItemFactory.NUMBER, "1");
        itm.getMetadata().put("jrx-my-expr", new JRuleItemMetadata("X > 0", Collections.emptyMap()));
        itm.getMetadata().put("jrx", new JRuleItemMetadata("jrx-my-expr && 1 > 0", Collections.emptyMap()));

        assertEquals("(X > 0) && 1 > 0", new JrxItemExpression(itm).getXpr());
        assertTrue(itm.evaluateJrx());
    }

    private JrxItemExpression createJrxItemExpression(JrxItem item) {
        return new JrxItemExpression(item, itemRegistry, functionRegistry) ;
    }
}
