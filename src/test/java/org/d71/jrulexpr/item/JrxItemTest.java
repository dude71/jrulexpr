package org.d71.jrulexpr.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.d71.jrulexpr.expression.AbstractJrxExpressionTest;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;

@ExtendWith(MockitoExtension.class)
public class JrxItemTest extends AbstractJrxExpressionTest {
    @Mock
    ItemRegistry itemRegistry;

    @Mock
    JrxFunctionRegistry functionRegistry;

    @Mock
    private Item itm;

    @BeforeEach
    @Override
    public void init() {
    }

    @Test
    public void getJrxExisting() {
        JrxItem itm = createJrxItem("A", CoreItemFactory.NUMBER, "1", "jrx=1 > 2");
        assertEquals("1 > 2", itm.getJrx());
    }

    @Test
    public void getJrxNonExisting() {
        JrxItem itm = createJrxItem("A", CoreItemFactory.NUMBER, "1");
        assertNull(itm.getJrx());
    }

    @Test
    public void getFunctions() {
    }

}
