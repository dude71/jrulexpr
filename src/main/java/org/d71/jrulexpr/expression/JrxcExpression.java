package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;

import java.util.Properties;
import java.util.stream.Stream;

public class JrxcExpression extends AbstractJRuleXprExpression<Properties> {
    private static final String REGEX_CSV = ",(?=(?:[^\']*\'[^\']*\')*[^\']*$)";

    JrxcExpression(JrxItem item) {
        super(item);
    }

    @Override
    public JRuleXprExpressionType getJrxType() {
        return JRuleXprExpressionType.JRXC;
    }

    @Override
    protected Object evaluateExpression() {
        String jrxc = getDefinition().orElseThrow(() -> new IllegalStateException("jrxc missing!"));
        Properties props = new Properties();
        Stream.of(jrxc.split(REGEX_CSV, -1)).map(String::trim).forEach(ps -> {
            String name = ps.replaceFirst( "\s*=.*$", "");
            // no null value possible in Properties, so use empty string to indicate key with no value
            String val = name.equals(ps) ? "" : ps.replaceFirst("^.*\s*=\s*", "");
            // strip single quotes around value if present
            if (val.startsWith("'") && val.endsWith("'")) {
                val = val.substring(1, val.length() - 1);
            }
            props.put(name, val);
        });
        return props;
    }

    @Override
    protected Properties defaultValue() {
        return new Properties();
    }

    @Override
    protected Properties convertEvaluatedValue(Object valueObj) {
        if (valueObj instanceof Properties) {
            return (Properties)valueObj;
        } else {
            throw valueConversionException(valueObj);
        }
    }
}
