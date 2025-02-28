package org.d71.jrulexpr.function;

import com.ezylang.evalex.functions.AbstractFunction;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractItemChangeFunction<V> extends AbstractFunction implements JrxFunction<V> {
    private String itemName;

    protected JrxItem item;
    protected JrxItemRegistry itemRegistry;

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public void setParameters(List<Object> values) {
        if (values.size() == 1) {
            itemName = (String) values.get(0);
        }
    }

    @Override
    public Set<RuleTrigger> getRuleTriggers() {
        return Collections.singleton(new RuleTrigger() {
            @Override
            public boolean evaluateOnChange() {
                // only when itemName of function "call" equals jrx item add JRule OnUpdate trigger
                return itemName == null || itemName.equals(item.getName());
            }

            @Override
            public String getItemName() {
                return itemName == null ? item.getName() : itemName;
            }
        });
    }

    protected boolean selfTriggered() {
        JRuleEvent evt = item == null ? null : item.getLastTriggeredBy();
        return evt instanceof JRuleItemEvent jrEvt && item.getName().equals(jrEvt.getItem().getName());
    }

}
