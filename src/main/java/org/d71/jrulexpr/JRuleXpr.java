package org.d71.jrulexpr;

import java.util.List;

import org.d71.jrulexpr.item.ItemUtil;
import org.d71.jrulexpr.rule.ItemRuleGenerator;

import org.openhab.core.items.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JRuleXpr {
    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXpr.class);

    private static JRuleXpr instance = null;

    public static JRuleXpr getInstance() {
        return instance == null ? (instance = new JRuleXpr()) : instance;
    }

    private ItemRuleGenerator itemRuleGenerator = new ItemRuleGenerator();

    public void unload() {
        LOGGER.info("## Unloading JRuleXpr..");
        instance = null;
    }

    public void generateItemRules() {
        LOGGER.info("## Starting JRuleXpr..");
        try {
            List<Item> jrxItems = ItemUtil.getItemNames();
            jrxItems.forEach(itemRuleGenerator::generate); 
            itemRuleGenerator.makeAll();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}
