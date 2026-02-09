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
    public void testTwoItemsNotNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, "ON");
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, "2");

        assertTrue(func.getValue(new Object[] { itm.getState(), itm2.getState() }));
    }

    @Test
    public void testTwoItemsNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, null);

        assertFalse(func.getValue(new Object[] { itm.getState(), itm2.getState() }));
    }

    @Test
    public void testThreeItemsNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, null);
        JrxItem itm3 = createMockedItem("dm", CoreItemFactory.DIMMER, null);

        assertFalse(func.getValue(new Object[] { itm.getState(), itm2.getState(), itm3.getState() }));
    }

    @Test
    public void testThreeItemsTwoNull() {
        NotNull func = new NotNull();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("sw", CoreItemFactory.SWITCH, null);
        JrxItem itm2 = createMockedItem("nr", CoreItemFactory.NUMBER, "1");
        JrxItem itm3 = createMockedItem("dm", CoreItemFactory.DIMMER, null);

        assertFalse(func.getValue(new Object[] { itm.getState(), itm2.getState(), itm3.getState() }));
    }

}
