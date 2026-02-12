package org.d71.jrulexpr.item;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openhab.automation.jrule.exception.JRuleItemNotFoundException;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxItemRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxItemRegistry.class);

    private static final Set<String> META_TAGS = Set.of("jrx", "jrxp", "jrxt", "jrxf");

    private static JrxItemRegistry jrxItemRegistry;

    public static synchronized JrxItemRegistry getInstance() {
        if (jrxItemRegistry == null) {
            jrxItemRegistry = new JrxItemRegistry();
        }
        return jrxItemRegistry;
    }

    private static ItemRegistry getItemRegistry() {
        return JRuleEventHandler.get().getItemRegistry();
    }

    private static final Predicate<JRuleItem> hasJrxMetadata = i -> {
       return META_TAGS.stream().anyMatch(m -> i.getMetadata().containsKey(m));
    };

    public Set<JrxItem> getRuleItems() {
        return getItemRegistry().getItems()
            .stream()
            .map(Item::getName)
            .map(this::getJRuleItem)
            .filter(hasJrxMetadata)
            .map(JrxItem::new)
            .collect(Collectors.toSet());
    }

    public JrxItem getItem(String name) {
        try {
            return new JrxItem(getJRuleItem(name));
        } catch (JRuleItemNotFoundException e) {
            LOGGER.warn("JRuleItem {} not found!", name);
            throw e;
        }        
    }

    private JRuleItem getJRuleItem(String name) throws JRuleItemNotFoundException {
        LOGGER.debug("getJRuleItem {}", name);
        JRuleItem itm = JRuleItem.forName(name);
        LOGGER.debug("JRuleItem {} state={}", new Object[] { itm, itm.getState() });
        return itm;
    }

    public static List<String> getGroupNames(JrxItem item) {
        // JRuleItem.getGroupItems throws JRuleItemNotFoundException when group not
        // defined as Group item in OH!
        return getItemRegistry().get(item.getName()).getGroupNames();
    }
}
