package org.d71.jrulexpr;

import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.ItemRuleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JRuleXpr {
    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXpr.class);

    private static JRuleXpr instance = null;

    public static JRuleXpr getInstance() {
        return instance == null ? (instance = new JRuleXpr()) : instance;
    }

    private final JrxItemRegistry itemRegistry = JrxItemRegistry.getInstance();
    private final ItemRuleGenerator itemRuleGenerator = new ItemRuleGenerator();

    public void unload() {
        LOGGER.info("## Unloading JRuleXpr..");
        instance = null;
    }

    public void generateItemRules() {
        LOGGER.info("## Starting JRuleXpr..");
        try {
            itemRegistry.getItems().forEach(itemRuleGenerator::generate);
            itemRuleGenerator.makeAll();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}
