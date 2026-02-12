package org.d71.jrulexpr.item;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.CaseUtils;
import org.d71.jrulexpr.expression.JrxItemExpression;
import org.d71.jrulexpr.expression.JrxfItemExpression;
import org.d71.jrulexpr.expression.JrxpItemExpression;
import org.d71.jrulexpr.expression.JrxtItemExpression;
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
        if (this == otherState)
            return true;
        if (otherState == null)
            return false;
        if (getState() == null)
            return false;
        if (getState().getClass() != otherState.getClass())
            return false;
        return getState().equals(otherState);
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
        items.remove(this); // item cannot trigger JrxRule itself
        return items;
    }

    public Set<JrxItem> getItems() {
        Set<JrxItem> items = new HashSet<>(createJrxItemExpression().getItems());
        items.addAll(createJrxpItemExpression().getItems());
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

    public String getRuleClassName() {
        String clz = getJrxcValue("ruleClass");
        return clz == null ? item.getType() + "Rules" : clz;
    }

    public Boolean evaluateJrxp() {
        Boolean jrxpEval = createJrxpItemExpression().evaluateToBoolean();
        LOGGER.debug("eval {} jrxp {} -> {}", new Object[]{getName(), getJrxp(), jrxpEval});
        return jrxpEval;
    }

    public Boolean evaluateJrx() {
        Boolean jrxEval = createJrxItemExpression().evaluateToBoolean();
        LOGGER.debug("eval {} jrx {} -> {}", new Object[]{getName(), getJrx(), jrxEval});
        return jrxEval;
    }

    public JRuleValue evaluateJrxf() {
        Object eval = createJrxfItemExpression().evaluate();
        LOGGER.debug("eval {} jrxf {} -> {}", new Object[]{getName(), getJrxf(), eval});
        return ValueConverter.convertObjectToValue(eval, getType());
    }

    public JRuleValue evaluateJrxt() {
        Object eval = createJrxtItemExpression().evaluate();
        LOGGER.debug("eval {} jrxt {} -> {}", new Object[]{getName(), getJrxt(), eval});
        return ValueConverter.convertObjectToValue(eval, getType());
    }

    public JRuleValue evaluateNewState() {
        JRuleValue value;
        String methodName = getRuleMethodName();

        if (evaluateJrxp()) {
            value = getJrx() == null || evaluateJrx() ? evaluateJrxt() : (skipJrxf() ? item.getState() : evaluateJrxf());
        } else {
            LOGGER.debug("-- pre condition {} NOT met for {}", new Object[]{getJrxp(), methodName});
            value = item.getState();
        }
        return value;
    }

    public JRuleValue getState() {
        return item.getState();
    }

    public void send(JRuleValue value) {
        JRuleValue curr = getState();

        if (curr == null || !curr.equals(value) || forceCmd()) {
            LOGGER.info("{} -> {} ({})", new Object[]{value, item.getName(), item.getType()});
            if (value == null) {
                item.postNullUpdate();
            } else {
                item.sendUncheckedCommand(value);
            }
        } else {
            LOGGER.info("skip {} -> {} (curr={})", new Object[]{value, getName(), curr});
        }
    }

    public String getRuleMethodName() {
        return CaseUtils.toCamelCase(getName(), false, '_', '-', ' ');
    }

    public String getJrxcValue(String config) {
        String val = getJrxcConfigs().stream().filter(c -> c.matches("^" + config + "\s*=.*$")).findFirst().orElse(null);
        if (val != null) {
            val = val.replaceFirst(config + "\s*=\s*", "");
        }
        return val;
    }

    public Set<String> getJrxcValues(String config) {
        Set<String> rv;
        String val = getJrxcValue(config);
        if (val != null) {
            val = val.replaceFirst("^'", "").replaceFirst("'$", "");
            rv = Stream.of(val.split(",", -1)).map(String::trim).collect(Collectors.toSet());
        } else {
            rv = Collections.emptySet();
        }
        return rv;
    }

    public Map<String, String> getJrxVars() {
        Map<String, JRuleItemMetadata> metadataEntries = getMetadataEntries("jrx-\\S+");
        return metadataEntries.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue()));
    }

    public Optional<String> getJrxVar(String name) {
        return Optional.ofNullable(getJrxVars().get(name));
    }

    public String getCron() {
        String cron = getJrxcValue("cron");
        cron = cron == null ? null : cron.replaceFirst("^\'", "").replaceFirst("\'$", "");
        return cron == null ? null : "\"" + cron + "\"";
    }

    public Set<String> getNoTrigger() {
        String noTrig = getJrxcValue("noTrigger");
        noTrig = noTrig == null ? null : noTrig.replaceFirst("^\'", "").replaceFirst("\'$", "");
        return noTrig == null ? Collections.emptySet() : Set.of(noTrig.split(",\\W*"));
    }

    public boolean skipJrxf() {
        return getJrxcConfigs().stream().anyMatch(c -> c.contains("skipJrxf"));
    }

    protected boolean forceCmd() {
        String forceCmd = getJrxcValue("forceCmd");
        return forceCmd != null && forceCmd.equalsIgnoreCase("true");
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

    private String getJrxc() {
        return getMetadataValue("jrxc").orElse(getTagValue("jrxc").orElse(null));
    }

    private List<String> getJrxcConfigs() {
        String jrxc = getJrxc();
        String regExCSV = ",(?=(?:[^\']*\'[^\']*\')*[^\']*$)";
        return jrxc == null ? Collections.emptyList() : Stream.of(jrxc.split(regExCSV, -1)).map(String::trim).toList();
    }

}
