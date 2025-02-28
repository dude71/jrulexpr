package org.d71.jrulexpr.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.automation.jrule.items.JRuleGroupItem;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.library.CoreItemFactory;

public class GroupTest extends AbstractItemTest {
    @Test
    public void testGetValueGroupTrigger() {
        // SUT
        Group func = new Group();

        JrxItem itm = createMockedItem("item", CoreItemFactory.NUMBER, "1");

        JRuleGroupItem grp = createMockedJRuleGroupItem("group");

        JRuleItemEvent evt = Mockito.mock(JRuleItemEvent.class);
        Mockito.when(evt.getItem()).thenReturn(grp);
        itm.setLastTriggeredBy(evt);

        func.setItem(itm);

        assertTrue(func.getValue(grp.getName()));
    }

    @Test
    public void testGetValueNonGroupTrigger() throws ItemNotFoundException {
        // SUT
        Group func = new Group();
        JrxItemRegistry itemReg = Mockito.mock(JrxItemRegistry.class);
        func.setItemRegistry(itemReg);

        JrxItem itm = createMockedItem("item", CoreItemFactory.NUMBER, "1", "jrx=true");
        JRuleItem itmTr = createMockedJRuleItem("itemTr");
        JRuleItemEvent evt = Mockito.mock(JRuleItemEvent.class);
        Mockito.lenient().when(evt.getItem()).thenReturn(itmTr);
        Mockito.when(itemReg.getItem(itmTr.getName())).thenReturn(Mockito.mock(JrxItem.class));
        itm.setLastTriggeredBy(evt);

        func.setItem(itm);

        assertFalse(func.getValue("group"));
    }

    @Test
    public void testGetRuleTrigger() {
        // SUT
        Group func = new Group();

        List<Object> groups = Arrays.asList("group");

        func.setParameters(groups);

        assertEquals(groups.get(0), func.getRuleTriggers().iterator().next().getGroupNames().iterator().next());
    }
}
