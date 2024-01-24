package org.d71.jrulexpr.item;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.CaseUtils;
import org.d71.jrulexpr.expression.AbstractJrxItemExpression;
import org.d71.jrulexpr.expression.JrxItemExpression;
import org.d71.jrulexpr.expression.JrxfItemExpression;
import org.d71.jrulexpr.expression.JrxpItemExpression;
import org.d71.jrulexpr.expression.JrxtItemExpression;
import org.d71.jrulexpr.function.JrxFunction;
import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxItem.class);

    private final Item item;

    private String lastTriggeredBy;

    protected JrxItem(Item item) {
        this.item = item;
    }

    public String getName() {
        return item.getName();
    }

    public String getType() {
        return item.getType();
    }

    public void setLastTriggeredBy(String by) {
        this.lastTriggeredBy = by;
    }

    public String getLastTriggeredBy() {
        return lastTriggeredBy;
    }

    public Set<JrxFunction<?>> getFunctions() {
        Set<JrxFunction<?>> functions = new HashSet<>(createJrxItemExpression().getFunctionInstances());
        functions.addAll(createJrxpItemExpression().getFunctionInstances());
        functions.addAll(createJrxtItemExpression().getFunctionInstances());
        functions.addAll(createJrxfItemExpression().getFunctionInstances());
        return functions;
    }

    public Set<Item> getTriggeringItems() {
        Set<Item> items = new HashSet<>(createJrxItemExpression().getItems());
        items.addAll(createJrxpItemExpression().getItems());
        return items;
    }

    public Set<Item> getItems() {
        Set<Item> items = getTriggeringItems();
        items.addAll(createJrxtItemExpression().getItems());
        items.addAll(createJrxfItemExpression().getItems());
        return items;
    }

    public String getJrx() {
        return getTagValue("jrx").orElse(null);
    }

    public String getJrxp() {
        return getTagValue("jrxp").orElse(null);
    }

    public String getJrxt() {
        return getTagValue("jrxt").orElse(null);
    }

    public String getJrxf() {
        return getTagValue("jrxf").orElse(null);
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

    public State evaluateJrxf() {
        Object eval = createJrxfItemExpression().evaluate();
        LOGGER.debug("eval {} jrxf {} -> {}", new Object[] { getName(), getJrxf(), eval });
        return toState(eval);        
    }

    public State evaluateJrxt() {
        Object eval = createJrxtItemExpression().evaluate();
        LOGGER.debug("eval {} jrxt {} -> {}", new Object[] { getName(), getJrxt(), eval });
        return toState(eval);        
    }

    public Optional<State> evaluateNewState() {
        Optional<State> state;
        String methodName = getRuleMethodName();

        if (evaluateJrxp()) {
            return Optional.of(evaluateJrx() ? evaluateJrxt() : evaluateJrxf());
        } else {
            state = Optional.empty();
            LOGGER.debug("-- pre condition {} NOT met for {}", new Object[] { getJrxp(), methodName });
        }
        return state;
    }

    public State getState() {
        return item.getState();
    }

    public void send(State state) {
        LOGGER.info("Command {} for item {} ({})", new Object[] { state, item.getName(), item.getType() });

        State curr = getState();

        if (curr == null || !curr.equals(state)) {
            if (item instanceof DimmerItem) {
                ((DimmerItem) item).send((PercentType) state);
            } else if (item instanceof NumberItem) {
                ((NumberItem) item).send((DecimalType) state);
            } else if (item instanceof SwitchItem) {
                ((SwitchItem) item).send((OnOffType) state);
            }
        } else {
            LOGGER.info("skipping send on {} curr={} old={}", new Object[] { getName(), curr, state });
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

    private State toState(Object state) {
        State rv = null;
        if (item.getType().equals(CoreItemFactory.DIMMER)) {
            rv = PercentType.valueOf(String.valueOf(((BigDecimal) state).intValue()));
        } else if (item.getType().equals(CoreItemFactory.NUMBER)) {
            rv = DecimalType.valueOf(String.valueOf(state));
        } else if (item.getType().equals(CoreItemFactory.SWITCH)) {
            rv = String.valueOf(state).equals("ON") ? OnOffType.ON : OnOffType.OFF;
        } else {
            LOGGER.warn("Cannot convert state " + state + "!");
        }
        return rv;
    }

    private Optional<String> getTagValue(String tagName) {
        Optional<String> tagVal = item.getTags().stream()
                .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
                .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }
}
