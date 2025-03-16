package org.d71.jrulexpr.function;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.library.CoreItemFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotNullTest extends AbstractItemTest {
    @Test
    public void testTwoItemsNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        Mockito.when(itemReg.get(itm.getName())).thenReturn(Optional.of(itm));
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, null);
        Mockito.when(itemReg.get(itm2.getName())).thenReturn(Optional.of(itm2));

        assertTrue(func.getValue(new Object[] { itm.getName(), itm2.getName() }));
    }

    @Test
    public void testThreeItemsNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        Mockito.when(itemReg.get(itm.getName())).thenReturn(Optional.of(itm));
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, null);
        Mockito.when(itemReg.get(itm2.getName())).thenReturn(Optional.of(itm2));
        JrxItem itm3 = createMockedItem("dm", CoreItemFactory.DIMMER, null);
        Mockito.when(itemReg.get(itm3.getName())).thenReturn(Optional.of(itm3));

        assertTrue(func.getValue(new Object[] { itm.getName(), itm2.getName(), itm3.getName() }));
    }

    @Test
    public void testThreeItemsTwoNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        Mockito.when(itemReg.get(itm.getName())).thenReturn(Optional.of(itm));
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, "1");
        Mockito.when(itemReg.get(itm2.getName())).thenReturn(Optional.of(itm2));
        JrxItem itm3 = createMockedItem("dm", CoreItemFactory.DIMMER, null);
        Mockito.lenient().when(itemReg.get(itm3.getName())).thenReturn(Optional.of(itm3));

        assertFalse(func.getValue(new Object[] { itm.getName(), itm2.getName(), itm3.getName() }));
    }

}
