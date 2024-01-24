package org.d71.jrulexpr.expression;

import java.util.Arrays;
import java.util.HashSet;

import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractJrxExpressionTest {
    @Mock
    ItemRegistry itemRegistry;

    JrxFunctionRegistry functionRegistry = JrxFunctionRegistry.getInstance();

    @BeforeEach
    public void init() {
        createMockedItem("nItem1", CoreItemFactory.NUMBER, "10");
        createMockedItem("nItem2", CoreItemFactory.NUMBER, "20");
        createMockedItem("sItem1", CoreItemFactory.STRING, "item1");
        createMockedItem("sItem2", CoreItemFactory.STRING, "item2");
    }
        
    protected Item createMockedItem(String name, String type, String value, String... tags) {
        try {
            Item itm = Mockito.mock(Item.class);
            Mockito.lenient().when(itm.getType()).thenReturn(type);
            Mockito.lenient().when(itm.getName()).thenReturn(name);

            if (CoreItemFactory.NUMBER.equals(type))
                Mockito.lenient().when(itm.getState()).thenReturn(DecimalType.valueOf(value));
            else if (CoreItemFactory.DIMMER.equals(type))
                Mockito.lenient().when(itm.getState()).thenReturn(PercentType.valueOf(value));
            else if (CoreItemFactory.SWITCH.equals(type))
                Mockito.lenient().when(itm.getState()).thenReturn(OnOffType.valueOf(value));

            Mockito.lenient().when(itm.getTags()).thenReturn(new HashSet<>(Arrays.asList(tags)));
            Mockito.lenient().when(itemRegistry.getItem(name)).thenReturn(itm);
            Mockito.lenient().when(itemRegistry.get(name)).thenReturn(itm);
            return itm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    JrxExpression createJrxExpression(String xpr) {
        return new JrxExpression(xpr, itemRegistry, functionRegistry);
    }    
}
