package org.d71.jrulexpr.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;

import com.ezylang.evalex.data.EvaluationValue;

public class InitialValueTest extends AbstractItemTest {
    @Test
    public void testGetValue() {
        InitialValue func = new InitialValue();

        JrxItem itm = createMockedItem("itm", CoreItemFactory.NUMBER, "5");
        func.setItem(itm);
        Mockito.when(itm.getTags()).thenReturn(new ArrayList<>());

        assertEquals(itm.getState(), (JRuleValue)func.getValue(itm.getName()));
        assertTrue(itm.getTags().size() == 1);
    }

    @Test
    public void testEvaluate() throws Exception {
        InitialValue func = new InitialValue();

        JrxItem itm = createMockedItem("itm", CoreItemFactory.NUMBER, "5");
        func.setItem(itm);
        Mockito.when(itm.getTags()).thenReturn(new ArrayList<>());

        assertEquals(
            EvaluationValue.numberValue(itm.getState().toOhState().as(DecimalType.class).toBigDecimal()), 
            func.evaluate(null, null));
        assertTrue(itm.getTags().size() == 1);
    }
}
