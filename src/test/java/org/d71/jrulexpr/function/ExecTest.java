package org.d71.jrulexpr.function;

import com.ezylang.evalex.data.EvaluationValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecTest {
    private static final String OUTPUT = "stdout & stderr of process";

    @Mock
    private ProcessBuilder mockProcessBuilder;
    @Mock
    private Process mockProcess;
    @Mock
    private ProcessHandle mockProcessHandle;
    @Mock
    private BufferedReader mockReader;
    private AutoCloseable closeable;
    private Exec exec;

    @BeforeEach
    void setup() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        Mockito.when(mockProcessBuilder.start()).thenReturn(mockProcess);
        Mockito.when(mockProcess.toHandle()).thenReturn(mockProcessHandle);
        Mockito.when(mockProcessHandle.descendants()).thenReturn(Stream.empty());
        Mockito.when(mockProcess.getOutputStream()).thenReturn(Mockito.mock(OutputStream.class));
        exec = new Exec() {
            @Override
            protected ProcessBuilder createProcessBuilder(boolean inShell, String... cmdAndArgs) {
                return mockProcessBuilder;
            }
            @Override
            protected BufferedReader createBufferedReader(InputStream inputStream) {
                return mockReader;
            }
        };
    }

    @AfterEach
    void breakdown() throws Exception {
        closeable.close();
    }

    @Test
    void getValue_NoError() throws Exception {
        Mockito.when(mockReader.readLine()).thenReturn(OUTPUT).thenReturn(null);
        Mockito.when(mockProcess.waitFor(Mockito.anyLong(), Mockito.any())).thenReturn(true);
        Mockito.when(mockProcess.exitValue()).thenReturn(0);

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(0, result.get(0).getNumberValue().intValue());
        assertEquals(OUTPUT + "\n", result.get(1).getStringValue());
    }

    @Test
    void getValue_Error() throws Exception {
        Mockito.when(mockReader.readLine()).thenReturn("error").thenReturn(null);
        Mockito.when(mockProcess.waitFor(Mockito.anyLong(), Mockito.any())).thenReturn(true);
        Mockito.when(mockProcess.exitValue()).thenReturn(1);

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(Exec.CODE_ERROR, result.get(0).getNumberValue().intValue());
        assertEquals("error\n", result.get(1).getStringValue());
    }

    @Test
    void getValue_Exception() throws Exception {
        Mockito.when(mockProcess.waitFor(Mockito.anyLong(), Mockito.any())).thenThrow(new RuntimeException("rte"));

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(Exec.CODE_ERROR, result.get(0).getNumberValue().intValue());
        assertEquals("rte", result.get(1).getStringValue());
    }

    @Test
    void getValue_Timeout() throws Exception {
        Mockito.when(mockProcess.waitFor(Mockito.anyLong(), Mockito.any())).thenReturn(false).thenReturn(true);

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(Exec.CODE_TIMEOUT, result.get(0).getNumberValue().intValue());
    }

    @Test
    @Disabled
    void getValue_ls() {
        Exec exec = new Exec();
        List<EvaluationValue> result = exec.getValue("ls -l /");

        assertEquals(0, result.get(0).getNumberValue().intValue());
        assertTrue((result.get(1).getStringValue()).contains("bin"));
    }

    @Test
    @Disabled
    void getValue_ls_NoShell() {
        Exec exec = new Exec();
        List<EvaluationValue> result = exec.getValue("ls", "-l", "/");

        assertEquals(0, result.get(0).getNumberValue().intValue());
        assertTrue((result.get(1).getStringValue()).contains("bin"));
    }
}
