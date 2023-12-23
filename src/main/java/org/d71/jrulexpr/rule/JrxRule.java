package org.d71.jrulexpr.rule;

import org.d71.jrulexpr.expression.ItemExpressionFactory;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.data.EvaluationValue;

import static org.d71.jrulexpr.expression.ItemExpressionType.*;

import org.d71.jrulexpr.expression.IItemExpression;

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
            String methodName = RuleUtil.getMethodName(itemName);
            LOGGER.info(">> {} triggered by {}", new Object[] {methodName, eventInfo(event)});
            
            IItemExpression jrxp = ItemExpressionFactory.getItemExpression(JRXP, itemName);
            if (jrxp.evaluate().getBooleanValue()) {
                LOGGER.debug("Pre condition {} met for {}", new Object[] {jrxp, methodName});
                EvaluationValue jrx = ItemExpressionFactory.getItemExpression(JRX, itemName).evaluate();
                LOGGER.debug("Action condition {} {} for {}", new Object[] {jrx, jrx.getValue(), methodName});
                EvaluationValue jrxv = ItemExpressionFactory.getItemExpression(jrx.getBooleanValue() ? JRXT : JRXF, itemName).evaluate();
                LOGGER.debug("State condition {} {} for {}", new Object[]{jrxv, jrxv.getValue(), methodName});
                (new ItemCommandor(itemName)).command(jrxv.getValue());
            } else {
                LOGGER.debug("-- {} pre condition NOT met", new Object[] {methodName});
            }
        } catch (Exception e) {
            LOGGER.error("ERROR: ", e);
        }
    }    
}
