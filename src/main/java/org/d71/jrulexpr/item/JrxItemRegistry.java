package org.d71.jrulexpr.item;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxItemRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxItemRegistry.class);

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
                .map(this::getItem).toList());
    }

    public JrxItem getItem(String name) {
        return new JrxItem(getJRuleItem(name));
    }

    public Optional<JrxItem> get(String name) {
        Item item = itemRegistry.get(name);
        return item == null ? Optional.empty() : Optional.of(getItem(item));
    }

    public static List<String> getGroupNames(JrxItem item) {
        // JRuleItem.getGroupItems throws JRuleItemNotFoundException when group not
        // defined as Group item in OH!
        return JRuleEventHandler.get().getItemRegistry().get(item.getName()).getGroupNames();
    }

    protected JRuleItem getJRuleItem(String name) {
        LOGGER.debug("get JRuleItem " + name);
        JRuleItem itm = JRuleItem.forName(name);
        LOGGER.debug("JRuleItem " + itm + " state=" + itm.getState());
        return itm;
    }

    private JrxItem getItem(Item item) {
        return getItem(item.getName());
    }
}
