package org.d71.jrulexpr.function;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.library.CoreItemFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnabledTest extends AbstractItemTest {
    @Test
    public void testEnabledWithOverride() {
        Enabled func = new Enabled();
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
        Enabled func = new Enabled();
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
        Enabled func = new Enabled();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, "ON");
        Mockito.when(itemReg.getItem(itm.getName())).thenReturn(itm);

        assertTrue(func.getValue(new Object[] {itm.getName()}));
    }
}
