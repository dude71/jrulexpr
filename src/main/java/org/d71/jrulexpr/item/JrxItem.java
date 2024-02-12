package org.d71.jrulexpr.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.CaseUtils;
import org.d71.jrulexpr.expression.JrxItemExpression;
import org.d71.jrulexpr.expression.JrxfItemExpression;
import org.d71.jrulexpr.expression.JrxpItemExpression;
import org.d71.jrulexpr.expression.JrxtItemExpression;
import org.d71.jrulexpr.function.JrxFunction;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxItem.class);

    private final JRuleItem item;

    private JRuleEvent lastTriggeredBy;

    protected JrxItem(JRuleItem item) {
        this.item = item;
    }

    public List<String> getGroupNames() {
        // JRuleItem.getGroupItems throws JRuleItemNotFoundException when group not
        // defined as Group item in OH!
        return JrxItemRegistry.getGroupNames(this);
    }

    public String getName() {
        return item.getName();
    }

    public String getType() {
        return item.getType();
    }

    public List<String> getTags() {
        return item.getTags();
    }

    public Optional<String> getTagValue(String tagName) {
        Optional<String> tagVal = getTags().stream()
                .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
                .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }

    public void setTagValue(String tagName, String value) {
        getTags().add(tagName + "=" + value);
    }

    public Map<String, JRuleItemMetadata> getMetadata() {
        return item.getMetadata();
    }

    public JRuleItemMetadata getMetadataEntry(String meta) {
        return getMetadata().get(meta);
    }

    public Optional<String> getMetadataValue(String meta) {
        JRuleItemMetadata mdat = getMetadataEntry(meta);
        return Optional.ofNullable(mdat == null ? null : (String) mdat.getValue());
    }

    public Map<String, Object> getMetadataConfig(String meta) {
        JRuleItemMetadata mdat = getMetadataEntry(meta);
        return mdat == null ? Collections.emptyMap() : mdat.getConfiguration();
    }

    public Optional<Object> getMetadataConfigValue(String meta, String key) {
        return Optional.ofNullable(getMetadataConfig(meta).get(key));
    }

    public void setLastTriggeredBy(JRuleEvent event) {
        this.lastTriggeredBy = event;
    }

    public JRuleEvent getLastTriggeredBy() {
        return lastTriggeredBy;
    }

    public Set<JrxFunction<?>> getFunctions() {
        Set<JrxFunction<?>> functions = new HashSet<>(createJrxItemExpression().getFunctionInstances());
        functions.addAll(createJrxpItemExpression().getFunctionInstances());
        functions.addAll(createJrxtItemExpression().getFunctionInstances());
        functions.addAll(createJrxfItemExpression().getFunctionInstances());
        return functions;
    }

    public Set<JrxItem> getTriggeringItems() {
        Set<JrxItem> items = new HashSet<>(createJrxItemExpression().getItems());
        items.addAll(createJrxpItemExpression().getItems());
        return items;
    }

    public Set<JrxItem> getItems() {
        Set<JrxItem> items = getTriggeringItems();
        items.addAll(createJrxtItemExpression().getItems());
        items.addAll(createJrxfItemExpression().getItems());
        return items;
    }

    public String getJrx() {
        // return getTagValue("jrx").orElse(getMetadataValue("jrx",
        // "jrx").orElse(null));
        return getMetadataValue("jrx").orElse(getTagValue("jrx").orElse(null));
    }

    public String getJrxp() {
        // return getTagValue("jrxp").orElse(getMetadataValue("jrx",
        // "jrxp").orElse(null));
        return getMetadataValue("jrxp").orElse(getTagValue("jrxp").orElse(null));
    }

    public String getJrxt() {
        // return getTagValue("jrxt").orElse(getMetadataValue("jrx",
        // "jrxt").orElse(null));
        return getMetadataValue("jrxt").orElse(getTagValue("jrxt").orElse(null));
    }

    public String getJrxf() {
        // return getTagValue("jrxf").orElse(getMetadataValue("jrx",
        // "jrxf").orElse(null));
        return getMetadataValue("jrxf").orElse(getTagValue("jrxf").orElse(null));
    }

    public Boolean evaluateJrxp() {
        Boolean jrxpEval = createJrxpItemExpression().evaluateToBoolean();
        LOGGER.debug("eval {} jrxp {} -> {}", new Object[] { getName(), getJrxp(), jrxpEval });
        return jrxpEval;
    }

    public Boolean evaluateJrx() {
        Boolean jrxEval = createJrxItemExpression().evaluateToBoolean();
        LOGGER.debug("eval {} jrx {} -> {}", new Object[] { getName(), getJrx(), jrxEval });
        return jrxEval;
    }

    public JRuleValue evaluateJrxf() {
        Object eval = createJrxfItemExpression().evaluate();
        LOGGER.debug("eval {} jrxf {} -> {}", new Object[] { getName(), getJrxf(), eval });
        return ValueConverter.convertToValue(eval, getType());
    }

    public JRuleValue evaluateJrxt() {
        Object eval = createJrxtItemExpression().evaluate();
        LOGGER.debug("eval {} jrxt {} -> {}", new Object[] { getName(), getJrxt(), eval });
        return ValueConverter.convertToValue(eval, getType());
    }

    public Optional<JRuleValue> evaluateNewValue() {
        Optional<JRuleValue> value;
        String methodName = getRuleMethodName();

        if (evaluateJrxp()) {
            value = Optional.of(getJrx() == null || evaluateJrx() ? evaluateJrxt() : evaluateJrxf());
        } else {
            value = Optional.empty();
            LOGGER.debug("-- pre condition {} NOT met for {}", new Object[] { getJrxp(), methodName });
        }
        return value;
    }

    public JRuleValue getState() {
        return item.getState();
    }

    public void send(JRuleValue value) {
        LOGGER.info("Command {} for item {} ({})", new Object[] { value, item.getName(), item.getType() });

        JRuleValue curr = getState();

        if (curr == null || !curr.equals(value)) {
            item.sendUncheckedCommand(value);
        } else {
            LOGGER.info("skipping send on {} curr={} new={}", new Object[] { getName(), curr, value });
        }
    }

    public String getRuleMethodName() {
        return CaseUtils.toCamelCase(getName(), false, '_', '-', ' ');
    }

    protected JrxItemExpression createJrxItemExpression() {
        return new JrxItemExpression(this);
    }

    protected JrxpItemExpression createJrxpItemExpression() {
        return new JrxpItemExpression(this);
    }

    protected JrxtItemExpression createJrxtItemExpression() {
        return new JrxtItemExpression(this);
    }

    protected JrxfItemExpression createJrxfItemExpression() {
        return new JrxfItemExpression(this);
    }

    // private Optional<String> getMetadataValue(String meta, String name) {
    // JRuleItemMetadata mdat= getMetadata().get(meta);
    // return Optional.ofNullable(mdat == null ? null :
    // (String)mdat.getConfiguration().get(name));
    // }

}
