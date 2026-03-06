package org.d71.jrulexpr.rule;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JrxRuleTest {

    static class DummyEvent extends JRuleEvent {
        // empty subclass, used to get a predictable simple name
    }

    @Test
    void eventInfo_withItemEvent_returnsFormattedString() {
        JRuleItemEvent itemEvent = mock(JRuleItemEvent.class);
        JRuleItem jrItem = mock(JRuleItem.class);

        when(itemEvent.getItem()).thenReturn(jrItem);
        when(jrItem.getName()).thenReturn("MyItem");
        when(itemEvent.getOldState()).thenReturn(JRuleOnOffValue.OFF);
        when(itemEvent.getState()).thenReturn(JRuleOnOffValue.ON);

        JrxRule rule = new JrxRule();
        String info = rule.eventInfo(itemEvent);
        assertEquals("MyItem: OFF->ON", info);
    }

    @Test
    void eventInfo_withNonItemEvent_returnsClassName() {
        JRuleEvent event = new DummyEvent();
        JrxRule rule = new JrxRule();
        assertEquals("DummyEvent", rule.eventInfo(event));
    }

    @Test
    void execRule_normalFlow_callsItemMethodsAndSendsNewState() {
        JrxRule rule = new JrxRule();
        JRuleEvent event = new DummyEvent();

        // prepare mocks
        JrxItemRegistry mockRegistry = mock(JrxItemRegistry.class);
        JrxItem mockItem = mock(JrxItem.class);
        JRuleValue mockValue = mock(JRuleValue.class);

        when(mockRegistry.getItem("foo")).thenReturn(mockItem);
        when(mockItem.getRuleMethodName()).thenReturn("methodName");
        when(mockItem.evaluateNewState()).thenReturn(mockValue);

        try (MockedStatic<JrxItemRegistry> registryMock = mockStatic(JrxItemRegistry.class)) {
            registryMock.when(JrxItemRegistry::getInstance).thenReturn(mockRegistry);

            rule.execRule("foo", event);

            verify(mockItem).setLastTriggeredBy(event);
            verify(mockItem).getRuleMethodName();
            verify(mockItem).evaluateNewState();
            verify(mockItem).send(mockValue);
        }
    }

    @Test
    void execRule_whenEvaluationThrows_exceptionIsCaughtAndSendNotCalled() {
        JrxRule rule = new JrxRule();
        JRuleEvent event = new DummyEvent();

        JrxItemRegistry mockRegistry = mock(JrxItemRegistry.class);
        JrxItem mockItem = mock(JrxItem.class);

        when(mockRegistry.getItem("foo")).thenReturn(mockItem);
        when(mockItem.getRuleMethodName()).thenReturn("m");
        when(mockItem.evaluateNewState()).thenThrow(new RuntimeException("oops"));

        try (MockedStatic<JrxItemRegistry> registryMock = mockStatic(JrxItemRegistry.class)) {
            registryMock.when(JrxItemRegistry::getInstance).thenReturn(mockRegistry);

            // should not propagate
            assertDoesNotThrow(() -> rule.execRule("foo", event));

            verify(mockItem).setLastTriggeredBy(event);
            verify(mockItem).evaluateNewState();
            verify(mockItem, never()).send(any());
        }
    }
}