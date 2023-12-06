package org.d71.jrulexpr.rule;

import java.util.Map;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.text.CaseUtils;
import org.burningwave.core.classes.AnnotationSourceGenerator;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.FunctionSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.JRuleName;
import org.openhab.automation.jrule.rules.JRuleWhenItemChange;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;

public class ItemRuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRuleGenerator.class);

    private static final String RULE_PKG = "org.openhab.automation.jrule.rules.user.generated";
    //private static final String RULE_PKG = "org.openhab.automation.jrule.generated.jrx";
    private static final String RULE_PATH = "../conf/automation/jrule/rules";
    //private static final String RULE_PATH = "../conf/automation/jrule/gen";

    private Map<String, ClassSourceGenerator> classes = new HashMap<>();
    private ItemRegistry itemRegistry = JRuleEventHandler.get().getItemRegistry();
    private ItemExprEvaluator itemExprEvaluator = new ItemExprEvaluator();

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
                .addOuterCodeLine("import java.util.List;")
                .addOuterCodeLine("import org.openhab.core.items.Item;")
                .addOuterCodeLine("import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;")
                .addOuterCodeLine("import " + LoggerFactory.class.getName() + ";")
                .addOuterCodeLine("import " + ItemExprEvaluator.class.getName() + ";")
                .addOuterCodeLine("import " + ItemCommandor.class.getName() + ";")
                .addOuterCodeLine("import " + EvaluationValue.class.getName() + ";\n")
                .addField(createVar(Logger.class, "LOGGER", "LoggerFactory.getLogger(" + name + ".class)")
                    .addModifier(Modifier.FINAL)
                    .addModifier(Modifier.STATIC))
                .expands(JRule.class);
        return classSourceGenerator;
    }

    private FunctionSourceGenerator createMethod(ClassSourceGenerator classSG, Item item) {
        LOGGER.info("Creating JRule method for: " + item.getName());
        String methodName = getMethodName(item);
        return FunctionSourceGenerator.create(methodName)
                .addModifier(Modifier.PUBLIC)
                .addAnnotation(AnnotationSourceGenerator
                        .create(JRuleName.class)
                        .addParameter(VariableSourceGenerator.create("\"" + methodName + "\"")))
                .addParameter(VariableSourceGenerator
                    .create(TypeDeclarationSourceGenerator
                        .create(JRuleItemEvent.class), "event"))
                .setReturnType(void.class);
    }

    private void createMethodAnnotations(FunctionSourceGenerator method, Item item) throws Exception {
        String jrx = getJrx(item);
        LOGGER.info("Creating method annotations for " + jrx);
        Expression expression = new Expression(jrx);
        List<Item> items = expression.getUndefinedVariables().stream()
                .map(v -> itemRegistry.get(v)).toList();

        items.forEach(i -> method.addAnnotation(
                AnnotationSourceGenerator
                        .create(JRuleWhenItemChange.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + i.getName() + "\""))));
    }

    private VariableSourceGenerator createVar(Class<?> clz, String name, String valAsStr) {
        return VariableSourceGenerator.create(clz, name).setValue(valAsStr).addModifier(Modifier.PRIVATE);
    }

    private String getMethodName(Item item) {
        return CaseUtils.toCamelCase(item.getName(), false, '_', '-', ' ');
    }

    private String getJrx(Item item) {
        return itemExprEvaluator.getJrx(item);
    }

    private void createMethodBody(FunctionSourceGenerator method, Item item) {
        method.addBodyCode(
            "try {\n" +
            "String methodName = \"" + getMethodName(item) + "\";\n" +
            "LOGGER.info(\"{} triggered by {}\", new Object[] {methodName, event.getItem().getName()});\n" +
            "EvaluationValue ev = (new ItemExprEvaluator()).eval(\"" + item.getName() + "\");\n" +
            "LOGGER.info(\"{} eval {}\", new Object[] {methodName, ev.getBooleanValue()});\n" +
            "(new ItemCommandor(\"" + item.getName() + "\")).command(ev.getBooleanValue());\n" +
            "} catch (Exception e) {\n" +
            "LOGGER.info(\"ERROR: \" + e.getLocalizedMessage());\n" +
            "}"
        ).addThrowable(TypeDeclarationSourceGenerator.create(Exception.class));
    }
}
