package org.d71.jrulexpr.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.d71.jrulexpr.expression.AbstractJrxExpressionTest;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;
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
        JrxItem itm = createMockedItem("A", CoreItemFactory.NUMBER, "1", "jrx=1 > 2");
        assertEquals("1 > 2", itm.getJrx());
    }

    @Test
    public void getJrxNonExisting() {
        JrxItem itm = createMockedItem("A", CoreItemFactory.NUMBER, "1");
        assertNull(itm.getJrx());
    }

    @Test
    public void getFunctions() {
    }

    @Test
    public void getJrxcValue() {
        JrxItem itm = createMockedItem("X", CoreItemFactory.NUMBER, "1");
        itm.getMetadata().put("jrxc", new JRuleItemMetadata("ruleClass = TestRules, forceCmd=true", Collections.emptyMap()));
        
        assertEquals("TestRules", itm.getJrxcValue("ruleClass"));
        assertEquals("true", itm.getJrxcValue("forceCmd"));
    }

    @Test
    public void getRuleClassName() {
        JrxItem itm = createMockedItem("X", CoreItemFactory.NUMBER, "1");
        itm.getMetadata().put("jrxc", new JRuleItemMetadata("ruleClass=TestRules , x=b", Collections.emptyMap()));
        
        assertEquals("TestRules", itm.getRuleClassName());
    }

}
