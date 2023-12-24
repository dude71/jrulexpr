package org.d71.jrulexpr.rule.functions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "hostOrIp")
public class HostFunction extends AbstractFunction {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameterValues)
            throws EvaluationException {
        String hostOrIp = parameterValues[0].getStringValue();
        return EvaluationValue.booleanValue(hostReachable(hostOrIp));
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
