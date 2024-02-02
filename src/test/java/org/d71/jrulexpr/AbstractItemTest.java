package org.d71.jrulexpr;

import org.d71.jrulexpr.expression.JrxItemExpression;
import org.d71.jrulexpr.expression.JrxfItemExpression;
import org.d71.jrulexpr.expression.JrxpItemExpression;
import org.d71.jrulexpr.expression.JrxtItemExpression;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.automation.jrule.items.JRuleGroupItem;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

import java.util.Arrays;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractItemTest {
    @Mock
    protected ItemRegistry itemRegistry;

    protected JrxFunctionRegistry functionRegistry = JrxFunctionRegistry.getInstance();

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

    protected JrxItem createJrxItem(String name, String type, String value, String... tags) {
        return createJrxItem(createMockedItem(name, type, value, tags));
    }

    protected JRuleItem createMockedJRuleItem(String name) {
        JRuleItem itm = Mockito.mock(JRuleItem.class);
        Mockito.lenient().when(itm.getName()).thenReturn(name);
        return itm;
    }

    protected JRuleGroupItem createMockedJRuleGroupItem(String name) {
        JRuleGroupItem grp = Mockito.mock(JRuleGroupItem.class);
        Mockito.lenient().when(grp.getName()).thenReturn(name);
        return grp;
    }

    private JrxItem createJrxItem(Item item) {
        return new JrxItem(item) {
            @Override
            protected JrxItemExpression createJrxItemExpression() {
                return new JrxItemExpression(this, itemRegistry, functionRegistry);
            }

            @Override
            protected JrxfItemExpression createJrxfItemExpression() {
                return new JrxfItemExpression(this, itemRegistry, functionRegistry);
            }

            @Override
            protected JrxpItemExpression createJrxpItemExpression() {
                return new JrxpItemExpression(this, itemRegistry, functionRegistry);
            }

            @Override
            protected JrxtItemExpression createJrxtItemExpression() {
                return new JrxtItemExpression(this, itemRegistry, functionRegistry);
            }
        };
    }
}
