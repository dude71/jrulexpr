package org.d71.jrulexpr.rule;

import static org.d71.jrulexpr.expression.ItemExpressionType.JRX;
import static org.d71.jrulexpr.expression.ItemExpressionType.JRXP;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.burningwave.core.classes.AnnotationSourceGenerator;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.FunctionSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;
import org.d71.jrulexpr.expression.IItemExpression;
import org.d71.jrulexpr.expression.ItemExpressionFactory;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.JRuleName;
import org.openhab.automation.jrule.rules.JRuleWhenCronTrigger;
import org.openhab.automation.jrule.rules.JRuleWhenItemChange;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.data.EvaluationValue;

public class ItemRuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRuleGenerator.class);

    private static final String RULE_PKG = "org.openhab.automation.jrule.rules.user.generated";
    // private static final String RULE_PKG =
    // "org.openhab.automation.jrule.generated.jrx";
    private static final String RULE_PATH = "../conf/automation/jrule/rules";
    // private static final String RULE_PATH = "../conf/automation/jrule/gen";

    private Map<String, ClassSourceGenerator> classes = new HashMap<>();

    private UnitSourceGenerator unitSG = UnitSourceGenerator.create(RULE_PKG);

    public ItemRuleGenerator() {
    }

    public void generate(Item item) {
        LOGGER.info("Generate rule for: " + item.getName() + " (" + item.getType() + ")");
        try {
            ClassSourceGenerator classSG = classes.get(item.getType());
            if (classSG == null) {
                classSG = createClass(item);
                classes.put(item.getType(), classSG);
            }
            FunctionSourceGenerator method = createMethod(classSG, item);
            createMethodAnnotations(method, item);
            createMethodBody(method, item);
            classSG.addMethod(method);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    public void makeAll() {
        LOGGER.info("Flushing rules");
        classes.values().forEach(clz -> {
            unitSG.addClass(clz);
            unitSG.make();
            unitSG.storeToClassPath(RULE_PATH);
            unitSG = UnitSourceGenerator.create(RULE_PKG);
        });
    }

    private ClassSourceGenerator createClass(Item item) {
        String name = item.getType() + "Rules";
        LOGGER.info("Creating new class: " + name);
        ClassSourceGenerator classSourceGenerator = ClassSourceGenerator
                .create(TypeDeclarationSourceGenerator.create(name))
                .addModifier(Modifier.PUBLIC)
                .addOuterCodeLine(createImport(List.class))
                .addOuterCodeLine(createImport(RuleUtil.class))
                .addOuterCodeLine(createImport(Item.class))
                .addOuterCodeLine(createImport(JRuleEventHandler.class))
                .addOuterCodeLine(createImport(LoggerFactory.class))
                .addOuterCodeLine(createImport(ItemRuleGenerator.class))
                .addOuterCodeLine(createImport(ItemCommandor.class))
                .addOuterCodeLine(createImport(EvaluationValue.class) + ";\n")
                .addField(createVar(Logger.class, "LOGGER", "LoggerFactory.getLogger(" + name + ".class)")
                        .addModifier(Modifier.FINAL)
                        .addModifier(Modifier.STATIC))
                .addField(createVar(ItemRegistry.class, "itemRegistry", "JRuleEventHandler.get().getItemRegistry()"))
                .addModifier(Modifier.FINAL)
                .expands(JrxRule.class);
        return classSourceGenerator;
    }

    private String createImport(Class<?> clz) {
        return "import " + clz.getName() + ";";
    }

    private FunctionSourceGenerator createMethod(ClassSourceGenerator classSG, Item item) {
        LOGGER.debug("Creating JRule method for: " + item.getName());
        String methodName = RuleUtil.getMethodName(item);
        return FunctionSourceGenerator.create(methodName)
                .addModifier(Modifier.PUBLIC)
                .addAnnotation(AnnotationSourceGenerator
                        .create(JRuleName.class)
                        .addParameter(VariableSourceGenerator.create("\"" + methodName + "\"")))
                .addParameter(VariableSourceGenerator
                        .create(TypeDeclarationSourceGenerator
                                .create(JRuleEvent.class), "event"))
                .setReturnType(void.class);
    }

    private void createMethodAnnotations(FunctionSourceGenerator method, Item item) throws Exception {
        LOGGER.debug("Creating method annotations for jrx");

        IItemExpression itemXpr = ItemExpressionFactory.getItemExpression(JRX, item.getName());
        Set<Item> items = itemXpr.getXprItems();

        if (LOGGER.isTraceEnabled()) {
            items.forEach(i -> LOGGER.trace("itm: " + i));
        }

        items.addAll(ItemExpressionFactory.getItemExpression(JRXP, item.getName()).getXprItems());

        items.stream().filter(i -> i != item).forEach(i -> method.addAnnotation(
                AnnotationSourceGenerator
                        .create(JRuleWhenItemChange.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + i.getName() + "\""))));

        Set<String> udFunctions = itemXpr.getXprFunctions();

        if (udFunctions.contains("HOUR")) {
            method.addAnnotation(AnnotationSourceGenerator
                    .create(JRuleWhenCronTrigger.class)
                    .addParameter("cron", VariableSourceGenerator.create("\"0 0 * * * *\"")));
        }

        if (udFunctions.contains("HOST")) {
            method.addAnnotation(AnnotationSourceGenerator
                    .create(JRuleWhenCronTrigger.class)
                    .addParameter("cron", VariableSourceGenerator.create("\"0 0/5 * * * *\"")));
        }

    }

    private VariableSourceGenerator createVar(Class<?> clz, String name, String valAsStr) {
        return VariableSourceGenerator.create(clz, name).setValue(valAsStr).addModifier(Modifier.PRIVATE);
    }

    private void createMethodBody(FunctionSourceGenerator method, Item item) {
        method.addBodyCode("execRule(\"" + item.getName() + "\", event);")
                .addThrowable(TypeDeclarationSourceGenerator.create(Exception.class));
    }
}
