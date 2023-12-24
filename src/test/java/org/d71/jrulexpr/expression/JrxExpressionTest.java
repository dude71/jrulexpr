package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;

import org.d71.jrulexpr.expression.JrxExpressionForTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.EvaluationValue.DataType;

@ExtendWith(MockitoExtension.class)
public class JrxExpressionTest {
    @Mock
    private ItemRegistry itemRegistry;

    @Test
    public void evalTwoConditionsAnd() throws Exception {
        String name = "DM_LIVINGROOM_BULB";
        getMockedItem("NR_HOME_PROX_C", CoreItemFactory.NUMBER, "1");
        getMockedItem("NR_KITCHEN_MOTION", CoreItemFactory.NUMBER, "1");
        getMockedItem(name, 
            CoreItemFactory.DIMMER, 
            "1", 
            "jrx=NR_HOME_PROX_C == 1 && NR_KITCHEN_MOTION == 1",
            "jrxt=IF(IF(NR_KITCHEN_MOTION == 1, NR_KITCHEN_MOTION, NR_HOME_PROX_C), 75, 65)"
            );

        // SUT    
        JrxExpression jrxExpression = new JrxExpressionForTest(name, itemRegistry);          
        EvaluationValue eval = jrxExpression.evaluate();

        assertEquals(DataType.BOOLEAN, eval.getDataType());
        assertTrue(eval.getBooleanValue());
    }

    @Test
    public void switchItem() throws Exception {
        Item itm = getMockedItem("SW_ITEM", CoreItemFactory.SWITCH, "ON", "jrx=SW_ITEM==\"ON\"");
        JrxExpression jrxExpression = new JrxExpressionForTest(itm.getName(), itemRegistry);
        EvaluationValue eval = jrxExpression.evaluate();
        assertTrue(eval.getBooleanValue());
    }

    @Test
    public void hourFunction() throws  Exception {
        int h = LocalTime.now().getHour();
        Item itm = getMockedItem("NR_ITM", CoreItemFactory.NUMBER, "1", "jrx=HOUR() == " + h);
        JrxExpression jrxExpression = new JrxExpressionForTest(itm.getName(), itemRegistry);
        EvaluationValue eval = jrxExpression.evaluate();
        assertTrue(eval.getBooleanValue());
    }

    @Test
    public void lockFunction() throws Exception {
        Item item = getMockedItem("ITM", CoreItemFactory.NUMBER, "1", "jrxp=LOCK(1)");
        JrxpExpression jrxpExpression = new JrxpExpressionForTest(item.getName(), itemRegistry);
        EvaluationValue eval = jrxpExpression.evaluate();
        assertTrue(eval.getBooleanValue()); // not locked
        eval = jrxpExpression.evaluate();
        assertFalse(eval.getBooleanValue()); // locked     
    }

    @Test
    public void hostFunction() throws Exception {
        Item itm = getMockedItem("itm", CoreItemFactory.NUMBER, "0", "jrx=HOST(\"localhost\")");
        JrxExpression jrxExpression = new JrxExpressionForTest(itm.getName(), itemRegistry);
        assertTrue(jrxExpression.evaluate().getBooleanValue());
    }

    @Test
    public void hostFunctionUnreachable() throws Exception {
        Item itm = getMockedItem("itm", CoreItemFactory.NUMBER, "0", "jrx=HOST(\"nokia-sb-8000.dmz.lan\")");
        JrxExpression jrxExpression = new JrxExpressionForTest(itm.getName(), itemRegistry);
        assertFalse(jrxExpression.evaluate().getBooleanValue());
    }
    
    private Item getMockedItem(String name, String type, String value, String... tags) throws Exception {
        Item itm = Mockito.mock(Item.class);
        Mockito.lenient().when(itm.getType()).thenReturn(type);
        Mockito.lenient().when(itm.getName()).thenReturn(name);

        if (CoreItemFactory.NUMBER.equals(type))
            Mockito.lenient().when(itm.getState()).thenReturn(DecimalType.valueOf(value));
        else if (CoreItemFactory.SWITCH.equals(type))
            Mockito.lenient().when(itm.getState()).thenReturn(OnOffType.valueOf(value));

        Mockito.lenient().when(itm.getTags()).thenReturn(new HashSet<>(Arrays.asList(tags)));
        Mockito.lenient().when(itemRegistry.getItem(name)).thenReturn(itm);
        Mockito.lenient().when(itemRegistry.get(name)).thenReturn(itm);
        return itm;
    }
}
