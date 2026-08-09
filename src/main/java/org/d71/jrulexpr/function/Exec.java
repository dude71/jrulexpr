package org.d71.jrulexpr.function;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "cmd")
public class Exec extends AbstractFunction implements JrxFunction<List<EvaluationValue>> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected ProcessBuilder processBuilder = null;

    protected ProcessBuilder getProcessBuilder(String cmd) {
        return processBuilder == null ? new ProcessBuilder("sh", "-c", cmd) : processBuilder;
    }

    protected BufferedReader bufferedReader = null;

    protected BufferedReader getBufferedReader(InputStream inputStream) {
        return bufferedReader == null ? new BufferedReader(new InputStreamReader(inputStream)) : bufferedReader;
    }

    @Override
    public final String getToken() {
        return "EXEC";
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        return EvaluationValue.arrayValue(getValue(parameterValues[0].getStringValue()));
    }

    @Override
    public List<EvaluationValue> getValue(Object... parameters) {
        return exec((String) parameters[0]);
    }

    private List<EvaluationValue> exec(String cmd) {
        List<EvaluationValue> result = new ArrayList<>(2);
        int exitCode;
        StringBuilder output = new StringBuilder();

        try {
            if (cmd == null || cmd.trim().isEmpty()) {
                throw new IllegalArgumentException("cmd parameter cannot be null or empty");
            }
            ProcessBuilder pb = getProcessBuilder(cmd);

            // Merges stderr into stdout so there is only one stream
            pb.redirectErrorStream(true);

            Process process = pb.start();

            String line;
            try (BufferedReader reader = getBufferedReader(process.getInputStream())) {
                while ((line = reader.readLine()) != null && output.length() < 1000) {
                    output.append(line);
                }
            }

            exitCode = process.waitFor();
        } catch (Exception e) {
            exitCode = 9;
            output.append(e.getMessage());
        }

        result.add(EvaluationValue.numberValue(BigDecimal.valueOf(exitCode)));
        result.add(EvaluationValue.stringValue(output.toString()));

        return result;
    }
}
