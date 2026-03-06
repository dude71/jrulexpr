package org.d71.jrulexpr.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.ZonedDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;
import org.openhab.automation.jrule.rules.value.JRuleValue;

class JrxItemTest {

    @Test
    void testEqualsAndNotEquals() {
        JRuleItem aItem = mock(JRuleItem.class);
        JRuleItem bItem = mock(JRuleItem.class);
        JRuleItem cItem = mock(JRuleItem.class);

        when(aItem.getName()).thenReturn("lamp");
        when(bItem.getName()).thenReturn("lamp");
        when(cItem.getName()).thenReturn("fan");

        JrxItem a = new JrxItem(aItem);
        JrxItem b = new JrxItem(bItem);
        JrxItem c = new JrxItem(cItem);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    void testStateEqualsNullsAndValues() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("i");
        // null state
        when(item.getState()).thenReturn(null);
        JrxItem ji = new JrxItem(item);
        assertTrue(ji.stateEquals(null));
        assertFalse(ji.stateEquals("notnull"));

        // non-null state
        JRuleValue v1 = mock(JRuleValue.class);
        JRuleValue v2 = mock(JRuleValue.class);
        when(item.getState()).thenReturn(v1);
        assertTrue(ji.stateEquals(v1));
        assertFalse(ji.stateEquals(v2));
    }

    @Test
    void testTagOperations() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("it");
        List<String> tags = new ArrayList<>(List.of("t=old", "foo=bar"));
        when(item.getTags()).thenReturn(tags);

        JrxItem ji = new JrxItem(item);

        // getTagValue
        assertEquals(Optional.of("old"), ji.getTagValue("t"));

        // setTagValue replaces existing
        ji.setTagValue("t", "new");
        assertTrue(item.getTags().contains("t=new"));
        assertFalse(item.getTags().contains("t=old"));

        // removeTag
        item.getTags().add("x=1");
        ji.removeTag("x");
        assertFalse(item.getTags().contains("x=1"));
    }

    @Test
    void testMetadataAndJrxcParsing() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("my_item-name");
        when(item.getType()).thenReturn("SomeType");

        JRuleItemMetadata jrxcMeta = mock(JRuleItemMetadata.class);
        String jrxcVal = "keys='a,b,c',cron='0,30 0/5 1-3 * * ?',skipJrxf,forceCmd=true,ruleClass=MyClass,noTrigger='n1,n2'";
        when(jrxcMeta.getValue()).thenReturn(jrxcVal);

        JRuleItemMetadata jrxVarMeta = mock(JRuleItemMetadata.class);
        when(jrxVarMeta.getValue()).thenReturn("v1");

        JRuleItemMetadata confMeta = mock(JRuleItemMetadata.class);
        Map<String, Object> confMap = Map.of("k", "vv");
        when(confMeta.getConfiguration()).thenReturn(confMap);

        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        metadata.put("jrxc", jrxcMeta);
        metadata.put("jrxv_var1", jrxVarMeta);
        metadata.put("meta", confMeta);

        when(item.getMetadata()).thenReturn(metadata);

        JrxItem ji = new JrxItem(item);

        // rule method name camel case
        assertEquals("myItemName", ji.getRuleMethodName());

        // jrxc parsing
        assertEquals("MyClass", ji.getJrxcValue("ruleClass"));
        Set<String> keys = ji.getJrxcValues("keys");
        assertEquals(Set.of("a", "b", "c"), keys);

        // cron quoting
        assertEquals("0,30 0/5 1-3 * * ?", ji.getCron());

        // noTrigger split
        assertEquals(Set.of("n1", "n2"), ji.getNoTrigger());

        // skipJrxf detection
        assertTrue(ji.skipJrxf());

        // forceCmd detection
        assertTrue(ji.forceCmd());

        // jrx vars mapping
        Map<String, String> vars = ji.getJrxVars();
        assertEquals("v1", vars.get("jrxv_var1"));
        assertEquals(Optional.of("v1"), ji.getJrxVar("jrxv_var1"));

        // metadata config retrieval
        assertEquals("vv", ji.getMetadataConfigValue("meta", "k").orElse(null));
    }

    @Test
    void testGetLastUpdatedAndPreviousStateDefaults() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("x");
        when(item.lastUpdated("inmemory")).thenReturn(Optional.of(ZonedDateTime.now()));
        when(item.getPreviousState(true, "inmemory")).thenReturn(Optional.empty());

        JrxItem ji = new JrxItem(item);

        assertNotNull(ji.getLastUpdated());
        assertNull(ji.getPreviousState());
    }

    @Test
    void testSendBehavior() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("sendItem");
        when(item.getType()).thenReturn("TypeA");

        JRuleValue v1 = mock(JRuleValue.class);
        JRuleValue v2 = mock(JRuleValue.class);

        // different state -> sendUncheckedCommand called
        when(item.getState()).thenReturn(v1);
        JrxItem ji = new JrxItem(item);
        ji.send(v2);
        verify(item).sendUncheckedCommand(v2);

        // null value -> postNullUpdate called
        reset(item);
        when(item.getName()).thenReturn("sendItem");
        when(item.getType()).thenReturn("TypeA");
        when(item.getState()).thenReturn(v1);
        ji = new JrxItem(item);
        ji.send(null);
        verify(item).postNullUpdate();

        // same state and no forceCmd -> no send
        reset(item);
        when(item.getName()).thenReturn("sendItem");
        when(item.getType()).thenReturn("TypeA");
        when(item.getState()).thenReturn(v1);
        ji = new JrxItem(item);
        ji.send(v1);
        verify(item, never()).sendUncheckedCommand(any());
        verify(item, never()).postNullUpdate();
    }

    @Test
    void testSendRespectsForceCmd() {
        JRuleItem item = mock(JRuleItem.class);
        when(item.getName()).thenReturn("fitem");
        when(item.getType()).thenReturn("TypeB");

        JRuleItemMetadata jrxcMeta = mock(JRuleItemMetadata.class);
        when(jrxcMeta.getValue()).thenReturn("forceCmd");
        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        metadata.put("jrxc", jrxcMeta);
        when(item.getMetadata()).thenReturn(metadata);

        JRuleValue v = mock(JRuleValue.class);
        when(item.getState()).thenReturn(v);

        JrxItem ji = new JrxItem(item);
        ji.send(v); // state equals but forceCmd=true -> should send
        verify(item).sendUncheckedCommand(v);
    }

    @Test
    void testSanitizeJrxvsAndGetJrxVarsWithDashes() {
        String in = "jrxv-var-one";
        String out = JrxItem.sanitizeJrxvs(in);
        assertEquals("jrxv_var_one", out);

        JRuleItem item = mock(JRuleItem.class);
        JRuleItemMetadata meta = mock(JRuleItemMetadata.class);
        when(meta.getValue()).thenReturn("val");
        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        metadata.put("jrxv-var1", meta);
        when(item.getMetadata()).thenReturn(metadata);

        JrxItem ji = new JrxItem(item);
        Map<String, String> vars = ji.getJrxVars();
        assertTrue(vars.containsKey("jrxv_var1"));
        assertEquals("val", vars.get("jrxv_var1"));
    }
}