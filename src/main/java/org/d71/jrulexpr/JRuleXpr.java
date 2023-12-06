package org.d71.jrulexpr;

import java.lang.reflect.Modifier;
import java.util.List;

import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.io.FileSystemItem;
import org.d71.jrulexpr.item.ItemsRetriever;
import org.d71.jrulexpr.rule.ItemRuleGenerator;
import org.openhab.automation.jrule.rules.JRule;

import org.openhab.core.items.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;

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
            List<Item> jrxItems = new ItemsRetriever().getItemNames();
            jrxItems.forEach(itemRuleGenerator::generate); 
            itemRuleGenerator.makeAll();

            // LOGGER.info("## trying usg..");
            // UnitSourceGenerator unitSG = UnitSourceGenerator
            //         .create("org.openhab.automation.jrule.rules.user.generated")
            //         .addClass(
            //                 ClassSourceGenerator.create(
            //                         TypeDeclarationSourceGenerator.create("MyGeneratedClazz"))
            //                         .addModifier(Modifier.PUBLIC)
            //                         .expands(JRule.class));
            // LOGGER.info("## usg: " + unitSG.toString());
            // String clazz = unitSG.make();
            // String dir = "../conf/automation/jrule/rules"; // System.getProperty("user.dir");
            // LOGGER.info(">> clazz: " + clazz + " >> dir: " + dir);
            // FileSystemItem fsi = unitSG.storeToClassPath(dir);
            // LOGGER.info("fsi: " + fsi.getAbsolutePath());

            // String expr = "NR_KITCHEN_TEMP <= 20 && NR_HOME_PROX_A == 1";

            // Expression expression = new Expression(expr);
            // expression.getUsedVariables().forEach(v -> LOGGER.info("var: " + v));

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}
