package org.d71.jrulexpr.function;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;
import org.openhab.core.library.CoreItemFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnabledFunctionTest  extends AbstractItemTest {
    @Test
    public void testEnabledWithOverride() {
        EnabledFunction func = new EnabledFunction();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, "ON");
        Mockito.when(itemReg.getItem(itm.getName())).thenReturn(itm);
        JrxItem itmO = createMockedItem("swO", CoreItemFactory.SWITCH, "ON");
        Mockito.when(itemReg.getItem(itmO.getName())).thenReturn(itmO);

        assertFalse(func.getValue(new Object[] {itm.getName(), itmO.getName()}));
    }

    @Test
    public void testEnabledWithOverrideOff() {
        EnabledFunction func = new EnabledFunction();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, "ON");
        Mockito.when(itemReg.getItem(itm.getName())).thenReturn(itm);
        JrxItem itmO = createMockedItem("swO", CoreItemFactory.SWITCH, "OFF");
        Mockito.when(itemReg.getItem(itmO.getName())).thenReturn(itmO);

        assertTrue(func.getValue(new Object[] {itm.getName(), itmO.getName()}));
    }

    @Test
    public void testEnabledWithNoOverride() {
        EnabledFunction func = new EnabledFunction();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, "ON");
        Mockito.when(itemReg.getItem(itm.getName())).thenReturn(itm);

        assertTrue(func.getValue(new Object[] {itm.getName()}));
    }
}
