package org.d71.jrulexpr.rule;

import java.math.BigDecimal;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCommandor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCommandor.class);

    private Item item;

    public ItemCommandor(String itemName) throws Exception {
        this.item = JRuleEventHandler.get().getItemRegistry().getItem(itemName);
    }

    public void command(Object value) {
        Object cmd = convertValue(value);
        LOGGER.debug("Command {} for item {} ({})", new Object[] {cmd, item.getName(), item.getType()});

        if (item instanceof DimmerItem) {
            ((DimmerItem)item).send((PercentType)cmd);
        } else if (item instanceof NumberItem) {
            ((NumberItem)item).send((DecimalType)cmd);
        } else if (item instanceof SwitchItem) {
            ((SwitchItem)item).send((OnOffType)cmd);
        }

    }

    protected Object convertValue(Object value) {
        Object rv;
        if (item instanceof DimmerItem) {
            rv = PercentType.valueOf(String.valueOf(((BigDecimal)value).intValue()));
        } else if (item instanceof NumberItem) {
            rv = DecimalType.valueOf(String.valueOf(value));
        } else if (item instanceof SwitchItem) {
            rv = String.valueOf(value).equals("ON") ? OnOffType.ON : OnOffType.OFF;
        } else {
            rv = value;
        }
        return rv;
    }

}
