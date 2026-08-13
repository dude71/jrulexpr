package org.d71.jrulexpr.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "cmd")
public class Exec extends AbstractExecFunction implements JrxFunction<List<EvaluationValue>> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public final String getToken() {
        return "EXEC";
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        Object[] parameters = new String[parameterValues.length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameters[i] = parameterValues[i].getStringValue();
        }
        return EvaluationValue.arrayValue(getValue(parameters));
    }

    @Override
    public List<EvaluationValue> getValue(Object... parameters) {
        boolean inShell = parameters.length == 1;
        String[] strParams = Arrays.copyOf(parameters, parameters.length, String[].class);
        Object[] result = exec(inShell, true, strParams);
        List<EvaluationValue> rv = new ArrayList<>(2);
        rv.add(EvaluationValue.numberValue(BigDecimal.valueOf((int)result[0])));
        rv.add(EvaluationValue.stringValue((String)result[1]));
        return rv;
    }
}
