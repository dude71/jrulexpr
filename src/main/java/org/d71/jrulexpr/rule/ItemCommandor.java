package org.d71.jrulexpr.rule;

import java.math.BigDecimal;
import java.util.Arrays;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCommandor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCommandor.class);

    private Item item;

    public ItemCommandor(String itemName) throws Exception {
        this.item = JRuleEventHandler.get().getItemRegistry().getItem(itemName);
    }

    public void command(boolean eval) {
        LOGGER.debug("Command for {} item with eval {}", new Object[] {item.getType(), eval});
        if (item instanceof DimmerItem) {
            ((DimmerItem)item).send(PercentType.valueOf(eval ? "50" : "0"));
        }
        else if (item instanceof NumberItem) {
            ((NumberItem)item).send(DecimalType.valueOf(eval ? "1" : "0"));
        }
    }

    public void command(BigDecimal value) { 
    }
}
