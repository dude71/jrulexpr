package org.d71.jrulexpr.function;

import java.util.List;
import java.util.Optional;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.rule.RuleTrigger;
import org.openhab.automation.jrule.rules.event.JRuleChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "channel")
public class ChannelFunction extends AbstractFunction implements JrxFunction<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFunction.class);
    
    private String channel;

    private JrxItem item;

    @Override
    public String getToken() {
        return "CHANNEL";
    }

    @Override
    public void setParameters(List<Object> values) {
        if (values.size() == 1) {
            channel = (String) values.get(0);
        }
    }

    @Override
    public Optional<RuleTrigger> getRuleTrigger() {
        return channel == null ? Optional.empty() : Optional.of(new RuleTrigger() {
            public String getChannel() {
                return channel;
            }
        });
    }

    @Override
    public void setItem(JrxItem item) {
        this.item = item;
    }

    @Override
    public String getValue(Object... parameters) {
        LOGGER.debug("item lastTriggeredBy " + item.getLastTriggeredBy());
        if (item.getLastTriggeredBy() != null && item.getLastTriggeredBy() instanceof JRuleChannelEvent) {
            JRuleChannelEvent evt = (JRuleChannelEvent)item.getLastTriggeredBy();
            LOGGER.debug("event {}, value {}", new Object[] { evt, evt.getEvent() });
            return evt.getEvent();
        }
        return null;
    }

    @Override
    public EvaluationValue evaluate(Expression arg0, Token arg1, EvaluationValue... arg2) throws EvaluationException {
        String val = getValue();
        return val == null ? EvaluationValue.nullValue() : EvaluationValue.stringValue(val);
    }

}
