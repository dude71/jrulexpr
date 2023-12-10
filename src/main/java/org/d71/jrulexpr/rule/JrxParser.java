package org.d71.jrulexpr.rule;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.stream.Streams;
import org.openhab.automation.jrule.items.JRuleItemRegistry;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;

public class JrxParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxParser.class);

    private static Optional<String> getTagValue(Item item, String tagName) {
        Optional<String> tagVal = item.getTags().stream()
            .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
            .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }

    public static Set<Item> getXprItems(Item item, ItemExprEvaluator evaluator, ItemRegistry itemRegistry) throws Exception {
        LOGGER.info("Getting expr items for " + item.getName());
        Expression expression = evaluator.getExpression(getJrx(item).orElse(null), item);   

        Set<Item> items = expression.getUndefinedVariables().stream()
                .map(v -> itemRegistry.get(v)).collect(Collectors.toSet());    
                
        for (String j : Arrays.asList("jrxp", "jrxt", "jrxf")) {
            String tagVal = getTagValue(item, j).orElse(null);
            LOGGER.debug(j + ": " + tagVal);
            if (tagVal == null) continue;
            evaluator.getExpression(tagVal, item).getUndefinedVariables().stream()
                .map(v -> itemRegistry.get(v)).filter(i -> !items.contains(i)).forEach(items::add);                       
        }

        return items;     
    }

    public static Optional<String> getJrxp(Item item) {
        return getTagValue(item, "jrxp");
    }

    public static Optional<String> getJrx(Item item) {
        return getTagValue(item, "jrx");
    }
    
    public static Optional<String> getJrxt(Item item) {
        return getTagValue(item, "jrxt");
    }

    public static Optional<String> getJrxf(Item item) {
        return getTagValue(item, "jrxf");
    }    
}
