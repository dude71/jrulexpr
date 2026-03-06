package org.d71.jrulexpr.rule;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static final String RULE_PKG = "org.openhab.automation.jrule.rules.user";

    public static final String EVERY_MIN = "0 * * * * *";

    private final String RULE_PATH;

    private final Map<String, ClassSourceGenerator> classes = new HashMap<>();

    public ItemRuleGenerator(String rulePath) {
        RULE_PATH = rulePath;
    }

    public void generate(JrxItem item) {
        // Needs at least jrxp, jrx or jrxt. Otherwise, no rule is generated.
        if (item.getJrxp().isEmpty() && item.getJrx().isEmpty() && item.getJrxt().isEmpty()) {
            LOGGER.warn("No rule generated for item {} because it has no jrxp or jrx expression", item.getName());
        } else {
            LOGGER.debug("Generating rule for item {} with jrxp: {} and jrx: {}", item.getName(), item.getJrxp(),
                    item.getJrx());
            generateItemRule(item);
        }
    }

    public void makeAll() {
        LOGGER.info("Flushing rules");
        for (ClassSourceGenerator csg : classes.values()) {
            UnitSourceGenerator unitSG = UnitSourceGenerator.create(RULE_PKG);
            unitSG.addClass(csg);
            unitSG.make();
            unitSG.storeToClassPath(RULE_PATH);
        }
    }

    private void generateItemRule(JrxItem item) {
        String ruleClassName = item.getRuleClassName();
        LOGGER.info("Generating JrxRule for: " + item.getName() + " (" + item.getType() + ") in " + ruleClassName);
        try {
            ClassSourceGenerator classSG = classes.get(ruleClassName);
            boolean firstRule = classSG == null;
            if (firstRule) {
                classSG = createClass(item);
                classes.put(ruleClassName, classSG);
            }
            Set<AnnotationSourceGenerator> annotations = createMethodAnnotations(item);
            if (annotations.isEmpty()) {
                LOGGER.warn("No triggers found for item {}. No rule will be created!", item.getName());
                if (firstRule) {
                    classes.remove(ruleClassName);
                }
            } else {
                FunctionSourceGenerator method = createMethod(item);
                annotations.forEach(method::addAnnotation);
                createMethodBody(method, item);
                classSG.addMethod(method);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ClassSourceGenerator createClass(JrxItem item) {
        String name = item.getRuleClassName();
        LOGGER.info("Creating new class: " + name);
        ClassSourceGenerator classSourceGenerator = ClassSourceGenerator
                .create(TypeDeclarationSourceGenerator.create(name))
                .addModifier(Modifier.PUBLIC)
                .addOuterCodeLine(createImport(List.class))
                .addOuterCodeLine(createImport(JRuleMemberOf.class))
                .addOuterCodeLine(createImport(JRuleEventHandler.class))
                .addOuterCodeLine(createImport(LoggerFactory.class))
                .addOuterCodeLine("\n")
                .addModifier(Modifier.FINAL)
                .expands(JrxRule.class);
        return classSourceGenerator;
    }

    private String createImport(Class<?> clz) {
        return "import " + clz.getName() + ";";
    }

    private FunctionSourceGenerator createMethod(JrxItem item) {
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

    private Set<AnnotationSourceGenerator> createMethodAnnotations(JrxItem item) {
        Set<String> noTrigger = item.getNoTrigger();
        Set<JrxFunction<?>> triggeringFunctions = item.getTriggeringFunctions();
        Set<AnnotationSourceGenerator> annotations = new HashSet<>();

        // items in jrxp/jrx
        annotations.addAll(createItemAnnotations(item, noTrigger));
        // function groups
        annotations.addAll(createFunctionGroupAnnotations(triggeringFunctions));
        // function items
        annotations.addAll(createFunctionItemAnnotations(triggeringFunctions, noTrigger));
        // function channels
        annotations.addAll(createFunctionChannelAnnotations(triggeringFunctions));
        // crons on item and functions
        annotations.addAll(getCronExpressions(item.getCron(), triggeringFunctions));

        return annotations;
    }

    private Set<AnnotationSourceGenerator> createItemAnnotations(JrxItem item, Set<String> noTrigger) {
        return item.getTriggeringItems().stream()
                .filter(i -> !noTrigger.contains(i.getName()))
                .map(i -> AnnotationSourceGenerator.create(JRuleWhenItemChange.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + i.getName() + "\"")))
                .collect(Collectors.toSet());
    }

    private Set<AnnotationSourceGenerator> getCronExpressions(String cron, Collection<JrxFunction<?>> functions) {
        Set<String> cronXprs = new HashSet<>();
        if (cron != null) {
            cronXprs.add(cron);
        }
        functions.stream()
                .map(JrxFunction::getRuleTriggers)
                .flatMap(Set::stream)
                .map(RuleTrigger::getCronExpression)
                .filter(Objects::nonNull)
                .forEach(cronXprs::add);

        if (cronXprs.contains(EVERY_MIN)) { // TODO when crons smaller than every min
            cronXprs.clear();
            cronXprs.add(EVERY_MIN);
        }

        return cronXprs.stream().map(c -> AnnotationSourceGenerator.create(
                JRuleWhenCronTrigger.class)
                .addParameter("cron", VariableSourceGenerator.create("\"" + c + "\""))).collect(Collectors.toSet());
    }

    private Set<AnnotationSourceGenerator> createFunctionGroupAnnotations(Collection<JrxFunction<?>> functions) {
        return functions.stream()
                .map(JrxFunction::getRuleTriggers)
                .flatMap(Set::stream)
                .map(RuleTrigger::getGroupNames)
                .filter(Predicate.not(Set::isEmpty))
                .flatMap(Collection::stream)
                .map(g -> AnnotationSourceGenerator
                        .create(JRuleWhenItemReceivedUpdate.class)
                        .addParameter("item", VariableSourceGenerator.create("\"" + g + "\""))
                        .addParameter("memberOf", VariableSourceGenerator.create("JRuleMemberOf.All")))
                .collect(Collectors.toSet());
    }

    private Set<AnnotationSourceGenerator> createFunctionItemAnnotations(Collection<JrxFunction<?>> functions,
            Set<String> noTrigger) {
        Set<AnnotationSourceGenerator> annotations = new HashSet<>();

        // on change
        functions.stream()
                .map(JrxFunction::getRuleTriggers)
                .flatMap(Set::stream)
                .filter(RuleTrigger::evaluateOnChange)
                .map(RuleTrigger::getItemName)
                .filter(Predicate.not(noTrigger::contains))
                .forEach(i -> {
                    annotations.add(AnnotationSourceGenerator
                            .create(JRuleWhenItemChange.class)
                            .addParameter("item", VariableSourceGenerator.create("\"" + i + "\"")));
                });

        // on update
        functions.stream()
                .map(JrxFunction::getRuleTriggers)
                .flatMap(Set::stream)
                .filter(RuleTrigger::evaluateOnUpdate)
                .map(RuleTrigger::getItemName)
                .filter(Predicate.not(noTrigger::contains))
                .forEach(i -> {
                    annotations.add(AnnotationSourceGenerator
                            .create(JRuleWhenItemReceivedUpdate.class)
                            .addParameter("item", VariableSourceGenerator.create("\"" + i + "\"")));
                });

        return annotations;
    }

    private Set<AnnotationSourceGenerator> createFunctionChannelAnnotations(Collection<JrxFunction<?>> functions) {
        return functions.stream()
                .map(JrxFunction::getRuleTriggers)
                .flatMap(Set::stream)
                .filter(t -> t.getChannel() != null)
                .map(RuleTrigger::getChannel)
                .map(c -> AnnotationSourceGenerator
                        .create(JRuleWhenChannelTrigger.class)
                        .addParameter("channel", VariableSourceGenerator.create("\"" + c + "\"")))
                .collect(Collectors.toSet());
    }

    private void createMethodBody(FunctionSourceGenerator method, JrxItem item) {
        method.addBodyCode("execRule(\"" + item.getName() + "\", event);")
                .addThrowable(TypeDeclarationSourceGenerator.create(Exception.class));
    }
}
