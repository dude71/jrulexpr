package org.d71.jrulexpr;

import java.util.Arrays;

import org.d71.jrulexpr.expression.JrxItemExpression;
import org.d71.jrulexpr.expression.JrxfItemExpression;
import org.d71.jrulexpr.expression.JrxpItemExpression;
import org.d71.jrulexpr.expression.JrxtItemExpression;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.automation.jrule.items.JRuleGroupItem;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.rules.value.JRuleDecimalValue;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;
import org.openhab.automation.jrule.rules.value.JRulePercentValue;
import org.openhab.core.library.CoreItemFactory;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractItemTest {
    @Mock
    protected JrxItemRegistry itemRegistry;

    protected JrxFunctionRegistry functionRegistry = JrxFunctionRegistry.getInstance();

    protected JrxItem createMockedItem(String name, String type, String value, String... tags) {
        try {
            JRuleItem jrItm = Mockito.mock(JRuleItem.class);
            JrxItem itm = new JrxItem(jrItm) {
                @Override
                protected JrxItemExpression createJrxItemExpression() {
                    return new JrxItemExpression(this, itemRegistry, functionRegistry);
                }
               
                @Override
                protected JrxpItemExpression createJrxpItemExpression() {
                    return new JrxpItemExpression(this, itemRegistry, functionRegistry);
                }

                @Override
                protected JrxtItemExpression createJrxtItemExpression() {
                    return new JrxtItemExpression(this, itemRegistry, functionRegistry);
                }      
                
                @Override
                protected JrxfItemExpression createJrxfItemExpression() {
                    return new JrxfItemExpression(this, itemRegistry, functionRegistry);
                }                
            };
            Mockito.lenient().when(jrItm.getType()).thenReturn(type);
            Mockito.lenient().when(jrItm.getName()).thenReturn(name);

            if (CoreItemFactory.NUMBER.equals(type)) {
                Mockito.lenient().when(jrItm.getState()).thenReturn(value == null ? null : new JRuleDecimalValue(value));    
            }
            else if (CoreItemFactory.DIMMER.equals(type)) {
                Mockito.lenient().when(jrItm.getState()).thenReturn(new JRulePercentValue(value));
            }
            else if (CoreItemFactory.SWITCH.equals(type)) {
                Mockito.lenient().when(jrItm.getState()).thenReturn(JRuleOnOffValue.getValueFromString(value));
            }

            Mockito.lenient().when(jrItm.getTags()).thenReturn(Arrays.asList(tags));
            Mockito.lenient().when(itemRegistry.getItem(name)).thenReturn(itm);
            return itm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected JRuleItem createMockedJRuleItem(String name) {
        JRuleItem itm = Mockito.mock(JRuleItem.class);
        Mockito.lenient().when(itm.getName()).thenReturn(name);
        return itm;
    }

    protected JRuleGroupItem<?> createMockedJRuleGroupItem(String name) {
        JRuleGroupItem<?> grp = Mockito.mock(JRuleGroupItem.class);
        Mockito.lenient().when(grp.getName()).thenReturn(name);
        return grp;
    }

}
