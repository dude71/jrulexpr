package org.d71.jrulexpr.function;

import com.ezylang.evalex.functions.AbstractFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public abstract class AbstractExecFunction extends AbstractFunction {
    public static final int CODE_ERROR = 1;
    public static final int CODE_TIMEOUT = 124;

    private static final long WAIT_FOR = 30L; // default process wait time in seconds
    private static final long POST_DESTROY_WAIT = 5L; // wait after destroyForcibly
    private static final int OUTPUT_MAX = 1000;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected ProcessBuilder createProcessBuilder(boolean inShell, String... cmdAndArgs) {
        ProcessBuilder rv;
        if (inShell) {
            StringBuilder sbCmd = new StringBuilder();
            for (int i=0; i < cmdAndArgs.length; i++) {
                sbCmd.append(i + 1 == cmdAndArgs.length ? cmdAndArgs[i] : (cmdAndArgs[i] + " "));
            }
            rv = new ProcessBuilder("sh", "-c", sbCmd.toString());
        } else {
            rv = new ProcessBuilder(cmdAndArgs);
        }
        return rv;
    }

    protected BufferedReader createBufferedReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    protected Object[] exec(boolean inShell, boolean withOutput, String... cmdAndArgs) {
        Object[] result = new Object[2];
        StringBuilder output = withOutput ? new StringBuilder(): null;

        try {
            if (cmdAndArgs == null || cmdAndArgs.length == 0) {
                throw new IllegalArgumentException("cmdAndArgs parameter cannot be null or empty");
            }
            ProcessBuilder pb = createProcessBuilder(inShell, cmdAndArgs);

            // Merges stderr into stdout so there is only one stream
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Close stdin since we don't write to it; this also allows some child processes to detect EOF.
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                LOGGER.debug("Failed to close process stdin stream", e);
            }

            if (process.waitFor(WAIT_FOR, TimeUnit.SECONDS)) {
                result[0] = process.exitValue();
            } else {
                process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                LOGGER.warn("Process timed out after {}s and was destroyed (cmd={})", WAIT_FOR, cmdAndArgs);
                // Wait a short time to ensure termination is registered
                if (!process.waitFor(POST_DESTROY_WAIT, TimeUnit.SECONDS)) {
                    LOGGER.warn("Process did not terminate within {}s after forcible destroy (cmd={})", POST_DESTROY_WAIT, cmdAndArgs);
                }
                result[0] = CODE_TIMEOUT;
            }
            if (withOutput) readProcessOutput(process, output);
        } catch (InterruptedException ie) {
            // Restore interrupt status and treat as an error
            Thread.currentThread().interrupt();
            LOGGER.error("Thread interrupted while executing command: {}", cmdAndArgs, ie);
            result[0] = CODE_ERROR;
            if (withOutput) output.append(ie.getMessage() == null ? ie.toString() : ie.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to execute command: {}", cmdAndArgs, e);
            result[0] = CODE_ERROR;
            if (withOutput) output.append(e.getMessage() == null ? e.toString() : e.getMessage());
        }

        if (withOutput) result[1] = output.toString();
        return result;
    }

    private void readProcessOutput(Process process, StringBuilder output) throws IOException {
        try (BufferedReader reader = createBufferedReader(process.getInputStream())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() + line.length() + 1 > OUTPUT_MAX) {
                    int remaining = OUTPUT_MAX - output.length();
                    if (remaining > 0) {
                        // append a truncated piece and an ellipsis
                        output.append(line, 0, remaining);
                    }
                    output.append("..."); // indicate truncation
                    break;
                }
                output.append(line).append('\n');
            }
        } catch (IOException e) {
            // Catch stream closed caused by sudden termination
            if (e.getMessage() != null && e.getMessage().contains("Stream closed")) {
                LOGGER.error("Process was destroyed; stream closed cleanly.");
            } else {
                throw e;
            }
        }
    }
}
