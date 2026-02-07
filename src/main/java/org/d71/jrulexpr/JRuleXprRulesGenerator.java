package org.d71.jrulexpr;

import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.ItemRuleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JRuleXprRulesGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprRulesGenerator.class);

    private static JRuleXprRulesGenerator instance;
    private final JrxItemRegistry itemRegistry = JrxItemRegistry.getInstance();
    private final ItemRuleGenerator itemRuleGenerator = new ItemRuleGenerator();

    public static synchronized JRuleXprRulesGenerator getInstance() {
        if (instance == null) {
            instance = new JRuleXprRulesGenerator();
        }
        return instance;
    }

    public void generateItemRules() {
        LOGGER.info("## Starting JRuleXprRulesGenerator..");
        try {
            itemRegistry.getRuleItems().forEach(itemRuleGenerator::generate);
            itemRuleGenerator.makeAll();
            LOGGER.info("## JRuleXprRulesGenerator done.");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}
