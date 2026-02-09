package org.d71.jrulexpr.function;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "cmd")
public class Exec extends AbstractFunction implements JrxFunction<String> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public final String getToken() {
        return "EXEC";
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        return EvaluationValue.stringValue(getValue(parameterValues[0].getStringValue()));
    }

    @Override
    public String getValue(Object... parameters) {
        return execReturnStdout((String) parameters[0]);
    }

    private String execReturnStdout(String cmd) {
        String rv = null;
        try {
            Process proc = java.lang.Runtime.getRuntime().exec(new String[] {"sh", "-c", cmd});
            int i = proc.waitFor();
            if (i == 0) {
                rv = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                rv = rv.replaceFirst("\\n", "");
                LOGGER.debug("rv=" + rv);
            } else
                LOGGER.warn("cmd " + cmd + ", return code " + i);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return rv;
    }

}
