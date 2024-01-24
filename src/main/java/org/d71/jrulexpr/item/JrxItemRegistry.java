package org.d71.jrulexpr.item;

import java.util.HashSet;
import java.util.Set;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

public class JrxItemRegistry {
    private final ItemRegistry itemRegistry;
    private static JrxItemRegistry jrxItemRegistry;

    public static synchronized JrxItemRegistry getInstance() {
        return getInstance(JRuleEventHandler.get().getItemRegistry());
    }

    public static synchronized JrxItemRegistry getInstance(ItemRegistry itemRegistry) {
        if (jrxItemRegistry == null) {
            jrxItemRegistry = new JrxItemRegistry(itemRegistry);
        }
        return jrxItemRegistry;
    }

    public JrxItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public Set<JrxItem> getItems() {
        return new HashSet<>(itemRegistry
                .getAll()
                .stream()
                .filter(i -> i.getTags().stream()
                        .filter(t -> t.contains("jrx")).count() > 0)
                .map(JrxItem::new).toList());
    }

    public JrxItem getItem(String name) {
        Item item = itemRegistry.get(name);
        if (item == null) throw new IllegalStateException("Item " + name + " not found!");
        return new JrxItem(item);
    }
}
