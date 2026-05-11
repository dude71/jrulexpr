package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;

class JrxvExpressionTest {
    private static final class JrxItemT extends JrxItem {
        protected JrxItemT(JRuleItem item) {
            super(item);
        }
    }

    @Test
    void getDefinitionDelegatesToItem() {
        JRuleItem ri = mock(JRuleItem.class);
        JRuleItemMetadata meta = mock(JRuleItemMetadata.class);
        when(meta.getValue()).thenReturn("21");

        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        metadata.put("jrxv_var1", meta);
        when(ri.getMetadata()).thenReturn(metadata);

        JrxItem ji = new JrxItemT(ri);
        JrxvExpression expr = new JrxvExpression(ji, "jrxv_var1");

        Optional<String> def = expr.getDefinition();
        assertTrue(def.isPresent());
        assertEquals("21", def.get());
    }

    @Test
    void evaluateCachesValueAndUsesCacheOnSubsequentCalls() {
        JRuleItem ri = mock(JRuleItem.class);
        JRuleItemMetadata meta = mock(JRuleItemMetadata.class);
        when(meta.getValue()).thenReturn("42");

        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        metadata.put("jrxv_var1", meta);
        when(ri.getMetadata()).thenReturn(metadata);

        JrxItem realJi = new JrxItemT(ri);
        JrxItem spyJi = spy(realJi);

        JrxvExpression expr = new JrxvExpression(spyJi, "jrxv_var1");

        Object first = expr.evaluate();
        assertNotNull(first);
        assertEquals("42", first.toString());

        // cached value should now be present
        Optional<Object> cached = spyJi.getJrxVarCachedValue("jrxv_var1");
        assertNotNull(cached);
        assertTrue(cached.isPresent());
        assertEquals(first.toString(), cached.get().toString());

        // evaluate again -> should retrieve from cache; putJrxVarValueInCache called only once
        Object second = expr.evaluate();
        assertEquals(first.toString(), second.toString());

        verify(spyJi, atLeastOnce()).putJrxVarValueInCache(eq("jrxv_var1"), any());
    }
}
