package org.d71.jrulexpr.expression;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.d71.jrulexpr.function.Hour;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.item.JrxItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @Test
    void evaluteFuncInVar() {
        JRuleItem ri = mock(JRuleItem.class);
        Map<String, JRuleItemMetadata> metadata = new HashMap<>();
        when(ri.getMetadata()).thenReturn(metadata);

        JRuleItemMetadata funcMeta= mock(JRuleItemMetadata.class);
        when(funcMeta.getValue()).thenReturn("HOUR()");
        metadata.put("jrx-H", funcMeta);
        JRuleItemMetadata meta = mock(JRuleItemMetadata.class);
        when(meta.getValue()).thenReturn("jrx-H <> 24");
        metadata.put("jrx-FinV", meta);
        meta = mock(JRuleItemMetadata.class);
        when(meta.getValue()).thenReturn("jrx-FinV");
        metadata.put("jrx", meta);

        JrxItem jrItm = new JrxItemT(ri);
        JrxExpression expr = new JrxExpression(jrItm);

        String def = expr.getDefinition().orElse(null);
        assertNotNull(def);

        assertTrue(expr.evaluate());
        Mockito.verify(funcMeta, Mockito.atLeastOnce()).getValue();
    }
}
