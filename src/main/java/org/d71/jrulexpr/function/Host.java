package org.d71.jrulexpr.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "hostOrIp")
public class Host extends AbstractFunction implements JrxFunction<Boolean> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public final String getToken() {
        return "HOST";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        String host = (String) parameters[0];
        boolean p = hostReachable(host);
        if (!p) {
            try {
                Thread.sleep(2000);
                p = hostReachable(host);
            } catch (InterruptedException e) {
            }
        }
        return p;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameterValues)
            throws EvaluationException {
        String hostOrIp = parameterValues[0].getStringValue();
        return EvaluationValue.booleanValue(getValue(hostOrIp));
    }

    private boolean hostReachable(String hostOrIp) {
        boolean rv = false;
        try {
            Process proc = java.lang.Runtime.getRuntime().exec("ping -c 1 " + hostOrIp);
            int i = proc.waitFor();
            rv = (i == 0);
            LOGGER.debug("Host " + hostOrIp + (rv ? "" : " NOT") + " reachable (i=" + i + ")");
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
            rv = false;
        }
        return rv;
    }
}
