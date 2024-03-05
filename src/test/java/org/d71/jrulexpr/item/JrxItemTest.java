package org.d71.jrulexpr.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

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

    @Test
    public void getJrxVars() {
        JrxItem itm = createMockedItem("X", CoreItemFactory.NUMBER, "1");
        itm.getMetadata().put("jrx-my-expr", new JRuleItemMetadata("X == 1", Collections.emptyMap()));
        itm.getMetadata().put("jrx-bla", new JRuleItemMetadata("10", Collections.emptyMap()));

        Map<String, String> jrxVars = itm.getJrxVars();
        assertEquals("X == 1", jrxVars.get("jrx-my-expr"));
    }

    @Test
    public void getJrxVar() {
        JrxItem itm = createMockedItem("X", CoreItemFactory.NUMBER, "1");
        itm.getMetadata().put("jrx-my-expr", new JRuleItemMetadata("B > 0", Collections.emptyMap()));

        assertTrue(itm.getJrxVar("jrx-abc").isEmpty());
        assertEquals("B > 0", itm.getJrxVar("jrx-my-expr").get());
    }

}
