package org.d71.jrulexpr.rule;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import org.apache.commons.text.CaseUtils;
import org.burningwave.core.classes.*;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.JRuleName;
import org.openhab.automation.jrule.rules.JRuleWhenCronTrigger;
import org.openhab.automation.jrule.rules.JRuleWhenItemChange;
import org.openhab.core.events.Event;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemRuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRuleGenerator.class);

    private static final String RULE_PKG = "org.openhab.automation.jrule.rules.user.generated";
    // private static final String RULE_PKG =
    // "org.openhab.automation.jrule.generated.jrx";
    private static final String RULE_PATH = "../conf/automation/jrule/rules";
    // private static final String RULE_PATH = "../conf/automation/jrule/gen";

    private Map<String, ClassSourceGenerator> classes = new HashMap<>();
    private ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();
    private ItemExprEvaluator itemExprEvaluator = new ItemExprEvaluator(itemRegistry);

    private UnitSourceGenerator unitSG = UnitSourceGenerator.create(RULE_PKG);

    public ItemRuleGenerator() {
    }

    public void generate(Item item) {
        LOGGER.info("Generate rule for: " + item.getName() + " " + item.getType());
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
                .addOuterCodeLine(createImport(ItemExprEvaluator.class))
                .addOuterCodeLine(createImport(ItemCommandor.class))
                .addOuterCodeLine(createImport(EvaluationValue.class) + ";\n")
                .addField(createVar(Logger.class, "LOGGER", "LoggerFactory.getLogger(" + name + ".class)")
                        .addModifier(Modifier.FINAL)
                        .addModifier(Modifier.STATIC))
                .addField(createVar(ItemRegistry.class, "itemRegistry", "JRuleEventHandler.get().getItemRegistry()"))
                .addModifier(Modifier.FINAL)
                .expands(JRule.class);
        return classSourceGenerator;
    }

    private String createImport(Class<?> clz) {
        return "import " + clz.getName() + ";";
    }

    private FunctionSourceGenerator createMethod(ClassSourceGenerator classSG, Item item) {
        LOGGER.info("Creating JRule method for: " + item.getName());
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
        String jrx = getJrx(item);
        LOGGER.info("Creating method annotations for " + jrx);
        Expression expression = itemExprEvaluator.getExpression(jrx, item);

        expression.getUndefinedVariables().forEach(v -> LOGGER.info("var: " + v));

        Set<Item> items = JrxParser.getXprItems(item, itemExprEvaluator, itemRegistry);

        items.forEach(i -> LOGGER.debug("itm: " + i.getName()));

        items.forEach(i -> method.addAnnotation(
                AnnotationSourceGenerator
                        .create(JRuleWhenItemChange.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + i.getName() + "\""))));

        expression.getAllASTNodes().forEach(n -> LOGGER.trace("node: " + n.getToken()));

        Set<String> udFunctions = itemExprEvaluator.getUdFunctions(expression);

        if (udFunctions.contains("HOUR")) {
            method.addAnnotation(AnnotationSourceGenerator
                    .create(JRuleWhenCronTrigger.class)
                    .addParameter("cron", VariableSourceGenerator.create("\"0 0 * * * *\"")));
        }

    }

    private VariableSourceGenerator createVar(Class<?> clz, String name, String valAsStr) {
        return VariableSourceGenerator.create(clz, name).setValue(valAsStr).addModifier(Modifier.PRIVATE);
    }

    private String getJrx(Item item) {
        return JrxParser.getJrx(item).orElse(null);
    }

    private void createMethodBody(FunctionSourceGenerator method, Item item) {
        method.addBodyCode("ItemRuleGenerator.callRuleMethod(\"" + RuleUtil.getMethodName(item) + "\", \"" + item.getName() + "\", event);")
                .addThrowable(TypeDeclarationSourceGenerator.create(Exception.class));
    }

    public static void callRuleMethod(String methodName, String itemName, JRuleEvent event) {
        try {
            LOGGER.info(">> {} triggered by {}", new Object[] {methodName, RuleUtil.eventInfo(event)});
            
            ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();
            ItemExprEvaluator evaluator = new ItemExprEvaluator(itemRegistry);
            if (evaluator.evalPre(itemName)) {
                LOGGER.debug("Pre condition met");
                EvaluationValue ev = evaluator.evalState(itemName);
                LOGGER.info("{} eval {}", new Object[] {methodName, ev});
                (new ItemCommandor(itemName)).command(ev.getValue());
            } else {
                LOGGER.info("{} pre condition not met", new Object[] {methodName});
            }
        } catch (Exception e) {
            LOGGER.error("ERROR: ", e);
        }
    }
}
