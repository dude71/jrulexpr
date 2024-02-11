package org.d71.jrulexpr.rule;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.burningwave.core.classes.AnnotationSourceGenerator;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.FunctionSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;
import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.item.JrxItem;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.rules.JRuleMemberOf;
import org.openhab.automation.jrule.rules.JRuleName;
import org.openhab.automation.jrule.rules.JRuleWhenChannelTrigger;
import org.openhab.automation.jrule.rules.JRuleWhenCronTrigger;
import org.openhab.automation.jrule.rules.JRuleWhenItemChange;
import org.openhab.automation.jrule.rules.JRuleWhenItemReceivedUpdate;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void generate(JrxItem item) {
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
            LOGGER.error(e.getClass().getSimpleName() + " " + e.getMessage());
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

    private ClassSourceGenerator createClass(JrxItem item) {
        String name = item.getType() + "Rules";
        LOGGER.info("Creating new class: " + name);
        ClassSourceGenerator classSourceGenerator = ClassSourceGenerator
                .create(TypeDeclarationSourceGenerator.create(name))
                .addModifier(Modifier.PUBLIC)
                .addOuterCodeLine(createImport(List.class))
                .addOuterCodeLine(createImport(JRuleMemberOf.class))
                .addOuterCodeLine(createImport(JRuleEventHandler.class))
                .addOuterCodeLine(createImport(LoggerFactory.class))
                .addOuterCodeLine("\n")
                .addField(createVar(Logger.class, "LOGGER", "LoggerFactory.getLogger(" + name + ".class)")
                        .addModifier(Modifier.FINAL)
                        .addModifier(Modifier.STATIC))
                .addModifier(Modifier.FINAL)
                .expands(JrxRule.class);
        return classSourceGenerator;
    }

    private String createImport(Class<?> clz) {
        return "import " + clz.getName() + ";";
    }

    private FunctionSourceGenerator createMethod(ClassSourceGenerator classSG, JrxItem item) {
        LOGGER.debug("Creating JRule method for: " + item.getName());
        String methodName = item.getRuleMethodName();
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

    private void createMethodAnnotations(FunctionSourceGenerator method, JrxItem item) throws Exception {
        LOGGER.debug("Creating method annotations for jrx");

        Set<JrxItem> items = item.getTriggeringItems();

        items.stream().filter(i -> i != item).forEach(i -> method.addAnnotation(
                AnnotationSourceGenerator
                        .create(JRuleWhenItemChange.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + i.getName() + "\""))));

        Set<JrxFunction<?>> functions = item.getFunctions();

        functions.stream()
                .map(JrxFunction::getRuleTrigger)
                .flatMap(Optional::stream)
                .map(RuleTrigger::getCronExpression)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()).forEach(c -> {
                    method.addAnnotation(AnnotationSourceGenerator
                            .create(JRuleWhenCronTrigger.class)
                            .addParameter("cron", VariableSourceGenerator.create(c)));
                });

        functions.stream()
                .map(JrxFunction::getRuleTrigger)
                .flatMap(Optional::stream)
                .map(RuleTrigger::getGroups)
                .filter(Predicate.not(Set::isEmpty))
                .flatMap(Collection::stream)
                .forEach(g -> {
                    method.addAnnotation(AnnotationSourceGenerator
                            .create(JRuleWhenItemReceivedUpdate.class)
                            .addParameter("item", VariableSourceGenerator.create("\"" + g + "\""))
                            .addParameter("memberOf", VariableSourceGenerator.create("JRuleMemberOf.All"))
                    );
                });

        functions.stream()
                .map(JrxFunction::getRuleTrigger)
                .flatMap(Optional::stream)
                .filter(RuleTrigger::evaluateOnUpdate)
                .map(RuleTrigger::getItem)
                .filter(item.getName()::equals)
                .collect(Collectors.toSet()).forEach(i -> {
                   method.addAnnotation(AnnotationSourceGenerator
                           .create(JRuleWhenItemReceivedUpdate.class)
                           .addParameter("item", VariableSourceGenerator.create("\"" + i + "\""))
                   );
                });

        functions.stream()
                .map(JrxFunction::getRuleTrigger)
                .flatMap(Optional::stream)
                .filter(RuleTrigger::evaluateOnChange)
                .map(RuleTrigger::getItem)
                .filter(item.getName()::equals)
                .collect(Collectors.toSet()).forEach(i -> {
                    method.addAnnotation(AnnotationSourceGenerator
                            .create(JRuleWhenItemChange.class)
                            .addParameter("item", VariableSourceGenerator.create("\"" + i + "\""))
                    );
                });

        functions.stream()
                .map(JrxFunction::getRuleTrigger)
                .flatMap(Optional::stream)
                .filter(t -> t.getChannel() != null)
                .map(RuleTrigger::getChannel)
                .forEach(c -> {
                    method.addAnnotation(AnnotationSourceGenerator
                        .create(JRuleWhenChannelTrigger.class)
                        .addParameter("channel", VariableSourceGenerator.create("\"" + c + "\""))
                    );
                });

    }

    private VariableSourceGenerator createVar(Class<?> clz, String name, String valAsStr) {
        return VariableSourceGenerator.create(clz, name).setValue(valAsStr).addModifier(Modifier.PRIVATE);
    }

    private void createMethodBody(FunctionSourceGenerator method, JrxItem item) {
        method.addBodyCode("execRule(\"" + item.getName() + "\", event);")
                .addThrowable(TypeDeclarationSourceGenerator.create(Exception.class));
    }
}
