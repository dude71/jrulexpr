package org.d71.jrulexpr.item;

import java.util.List;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Item;

public class ItemUtil {
    private static ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();

    public static synchronized List<Item> getItemNames() {
        return itemRegistry
            .getAll()
                .stream()
                    .filter(i -> i.getTags().stream()
                        .filter(t -> t.contains("jrx")).count() > 0).toList();
    }
}
