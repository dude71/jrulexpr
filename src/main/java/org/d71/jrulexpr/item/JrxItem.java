package org.d71.jrulexpr.item;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;
import org.d71.jrulexpr.expression.ExpressionFactory;
import org.d71.jrulexpr.function.JrxFunction;
import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.automation.jrule.items.JRuleItem;
import org.openhab.automation.jrule.items.metadata.JRuleItemMetadata;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxItem.class);

    private static final String DEFAULT_PERSISTENCE = "inmemory";

    private final JRuleItem item;

    private JRuleEvent lastTriggeredBy;

    protected JrxItem(JRuleItem item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JrxItem other = (JrxItem) obj;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }

    public boolean stateEquals(Object otherState) {
        JRuleValue state = getState();

        if (state == null && otherState == null) {
            return true;
        } else if (state == null || otherState == null) {
            return false;
        } else {
            return state.equals(otherState);
        }
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

    // tags not backed by OH tags!
    public List<String> getTags() {
        return item.getTags();
    }

    public Optional<String> getTagValue(String tagName) {
        Optional<String> tagVal = getTags().stream()
                .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
                .findFirst();
        return tagVal.map(s -> s.replaceFirst(tagName + "\s*=", ""));
    }

    public void setTagValue(String tagName, String value) {
        removeTag(tagName);
        getTags().add(tagName + "=" + value);
    }

    public void removeTag(String tagName) {
        Optional<String> tagVal = getTagValue(tagName);
        tagVal.ifPresent(s -> item.getTags().remove(tagName + "=" + s));
    }

    public Map<String, JRuleItemMetadata> getMetadata() {
        return item.getMetadata();
    }

    public JRuleItemMetadata getMetadataEntry(String meta) {
        return getMetadata().get(meta);
    }

    public Map<String, JRuleItemMetadata> getMetadataEntries(String regex) {
        return getMetadata().entrySet().stream().filter(e -> e.getKey().matches(regex)).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

    public JRuleValue getPreviousState() {
        return getPreviousState(DEFAULT_PERSISTENCE);
    }

    public JRuleValue getPreviousState(String persistence) {
        Optional<State> prevState = item.getPreviousState(true, persistence);
        return prevState.map(state -> JRuleEventHandler.get().toValue(state)).orElse(null);
    }

    public Long getLastUpdated() {
        return getLastUpdated(DEFAULT_PERSISTENCE);
    }

    public Long getLastUpdated(String persistence) {
        Optional<ZonedDateTime> lastUpd = item.lastUpdated(persistence);
        return lastUpd.isEmpty() ? null : lastUpd.get().toInstant().toEpochMilli();
    }

    public void setLastTriggeredBy(JRuleEvent event) {
        this.lastTriggeredBy = event;
    }

    public JRuleEvent getLastTriggeredBy() {
        return lastTriggeredBy;
    }

    public Set<JrxFunction<?>> getTriggeringFunctions() {
        Set<JrxFunction<?>> functions = new HashSet<>(ExpressionFactory.createJrxExpression(this).getReferencedFunctions(true));
        functions.addAll(ExpressionFactory.createJrxpExpression(this).getReferencedFunctions(true));
        functions.addAll(ExpressionFactory.createJrxtExpression(this).getReferencedFunctions(true));
        functions.addAll(ExpressionFactory.createJrxfExpression(this).getReferencedFunctions(true));
        return functions;
    }

    public Set<JrxItem> getTriggeringItems() {
        Set<JrxItem> items = new HashSet<>(ExpressionFactory.createJrxExpression(this).getReferencedItems(true));
        items.addAll(ExpressionFactory.createJrxpExpression(this).getReferencedItems(true));
        items.remove(this); // item cannot trigger JrxRule itself
        return items;
    }

    public Optional<String> getJrxc() {
        return getMetadataValue("jrxc");
    }

    public Optional<String> getJrxp() {
        return getMetadataValue("jrxp");
    }

    public Optional<String> getJrx() {
        return getMetadataValue("jrx");
    }

    public Optional<String> getJrxt() {
        return getMetadataValue("jrxt");
    }

    public Optional<String> getJrxf() {
        return getMetadataValue("jrxf");
    }

    public Map<String, String> getJrxVars() {
        Map<String, JRuleItemMetadata> metadataEntries = getMetadataEntries("jrx[v]?[_|-]\\S+");
        return metadataEntries.entrySet().stream()
            .collect(Collectors.toMap(e -> sanitizeJrxvs(e.getKey()), e -> sanitizeJrxvs(e.getValue().getValue())));
    }

    public static String sanitizeJrxvs(String def) {
        // Replace dashes with underscores in jrx(v)- definitions to ensure valid variable names in expressions.
        // "-" are interpreted as subtraction operators in expressions, which can lead to parsing errors if not handled.
        return def.replaceAll("(?<=\\bjrxv?[\\w-]*)-", "_");
    } 

    public Optional<String> getJrxVar(String name) {
        return Optional.ofNullable(getJrxVars().get(name));
    }     

    public String getRuleClassName() {
        String clz = getJrxcValue("ruleClass");
        return clz == null ? item.getType() + "Rules" : clz;
    }

    public Boolean evaluateJrxp() {
        Boolean jrxpEval = ExpressionFactory.createJrxpExpression(this).evaluate();
        LOGGER.debug("{}.jrxp: {} -> {}", new Object[] { getName(), getJrxp().orElse("<undef>"), jrxpEval });
        return jrxpEval;
    }

    public Boolean evaluateJrx() {
        Boolean value = ExpressionFactory.createJrxExpression(this).evaluate();
        LOGGER.debug("{}.jrx: {} -> {}", new Object[] { getName(), getJrx().orElse("<undef>"), value });
        return value;
    }

    public JRuleValue evaluateJrxf() {
        JRuleValue value = ExpressionFactory.createJrxfExpression(this).evaluate();
        LOGGER.debug("{}.jrxf: {} -> {}", new Object[] { getName(), getJrxf().orElse("<undef>"), value });
        return value;
    }

    public JRuleValue evaluateJrxt() {
        JRuleValue value = ExpressionFactory.createJrxtExpression(this).evaluate();
        LOGGER.debug("{}.jrxt: {} -> {}", new Object[] { getName(), getJrxt().orElse("<undef>"), value });
        return value;
    }

    public JRuleValue evaluateNewState() {
        JRuleValue value;

        if (evaluateJrxp()) {
            value = getJrx() == null || evaluateJrx() ? evaluateJrxt() : (skipJrxf() ? item.getState() : evaluateJrxf());
        } else {
            value = item.getState();
        }
        return value;
    }

    public JRuleValue getState() {
        return item.getState();
    }

    public void send(JRuleValue value) {
        if (!stateEquals(value) || forceCmd()) {
            LOGGER.info("{} -> {} ({})", new Object[]{value, item.getName(), item.getType()});
            if (value == null) {
                item.postNullUpdate();
            } else {
                item.sendUncheckedCommand(value);
            }
        } else {
            LOGGER.debug("skip {} -> {} (curr={})", new Object[]{value, getName(), getState()});
        }
    }

    public String getRuleMethodName() {
        return CaseUtils.toCamelCase(getName(), false, '_', '-', ' ');
    }

    public String getJrxcValue(String config) {
        Properties props = ExpressionFactory.createJrxcExpression(this).evaluate();
        return props.getProperty(config);
    }

    public Set<String> getJrxcValues(String config) {
        String val = getJrxcValue(config);
        return val == null || "".equals(val) ? Collections.emptySet() : Set.of(val.split(",\\W*"));
    }

    public String getCron() {
        return getJrxcValue("cron");
    }

    public Set<String> getNoTrigger() {
        return getJrxcValues("noTrigger");
    }

    public boolean skipJrxf() {
        String skipJrxf = getJrxcValue("skipJrxf");
        return "".equals(skipJrxf) || "true".equalsIgnoreCase(skipJrxf);
    }

    protected boolean forceCmd() {
        String forceCmd = getJrxcValue("forceCmd");
        return "".equals(forceCmd) || "true".equalsIgnoreCase(forceCmd);
    }

}
