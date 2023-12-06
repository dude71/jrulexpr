package org.d71.jrulexpr.item;

import java.util.List;

import org.openhab.automation.jrule.internal.JRuleConfig;
import org.openhab.automation.jrule.internal.JRuleConstants;
import org.openhab.automation.jrule.internal.JRuleUtil;
import org.openhab.automation.jrule.internal.compiler.JRuleCompiler;
import org.openhab.automation.jrule.internal.engine.JRuleEngine;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.internal.handler.JRuleHandler;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.internal.module.JRuleModuleUtil;
import org.openhab.automation.jrule.items.JRuleDimmerItem;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.items.JRuleItemRegistry;
import org.openhab.automation.jrule.items.JRuleNumberItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Item;

public class ItemsRetriever {
    private ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();

    public List<Item> getItemNames() {
        return itemRegistry
            .getAll()
                .stream()
                    .filter(i -> i.getTags().stream()
                        .filter(t -> t.contains("jrx")).count() > 0).toList();
    }
}
