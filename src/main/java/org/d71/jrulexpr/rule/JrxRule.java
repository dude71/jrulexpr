package org.d71.jrulexpr.rule;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxRule extends JRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxRule.class);

    protected String eventInfo(JRuleEvent event) {
        String rv;
        if (event instanceof JRuleItemEvent itemEvent) {
            rv = itemEvent.getItem().getName() + ", " + itemEvent.getOldState() + "->" + itemEvent.getState();
        } else {
            rv = event.getClass().getSimpleName();
        }
        return rv;
    }       

    protected void execRule(String itemName, JRuleEvent event) {
        try {
            JrxItem item = JrxItemRegistry.getInstance().getItem(itemName);
            item.setLastTriggeredBy(eventInfo(event));
            String methodName = item.getRuleMethodName();
            LOGGER.info(">> {} triggered by {}", new Object[] {methodName, item.getLastTriggeredBy()});
            
            item.evaluateNewState().ifPresent(item::send);
        } catch (Exception e) {
            LOGGER.error("ERROR: ", e);
        }
    }    
}
