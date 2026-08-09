package org.d71.jrulexpr.function;

import com.ezylang.evalex.data.EvaluationValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecTest {
    private static final String OUTPUT = "stdout & stderr of process";

    @Mock
    private ProcessBuilder mockProcessBuilder;
    @Mock
    private Process mockProcess;
    @Mock
    private BufferedReader mockReader;
    private AutoCloseable closeable;

    @BeforeEach
    void setup() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        Mockito.when(mockProcessBuilder.start()).thenReturn(mockProcess);
    }

    @AfterEach
    void breakdown() throws Exception {
        closeable.close();
    }

    @Test
    void getValue_NoError() throws Exception {
        Exec exec = new Exec();

        exec.processBuilder = mockProcessBuilder;
        exec.bufferedReader = mockReader;
        Mockito.when(mockReader.readLine()).thenReturn(OUTPUT).thenReturn(null);
        Mockito.when(mockProcess.waitFor()).thenReturn(0);

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(0, result.get(0).getNumberValue().intValue());
        assertEquals(OUTPUT, result.get(1).getStringValue());
    }

    @Test
    void getValue_Error() throws Exception {
        Exec exec = new Exec();

        exec.processBuilder = mockProcessBuilder;
        exec.bufferedReader = mockReader;
        Mockito.when(mockReader.readLine()).thenReturn("error").thenReturn(null);
        Mockito.when(mockProcess.waitFor()).thenReturn(1);

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(1, result.get(0).getNumberValue().intValue());
        assertEquals("error", result.get(1).getStringValue());
    }

    @Test
    void getValue_Exception() throws Exception {
        Exec exec = new Exec();

        exec.processBuilder = mockProcessBuilder;
        exec.bufferedReader = mockReader;
        Mockito.when(mockProcess.waitFor()).thenThrow(new InterruptedException("error!"));

        List<EvaluationValue> result = exec.getValue("some command");

        assertEquals(9, result.get(0).getNumberValue().intValue());
        assertEquals("error!", result.get(1).getStringValue());
    }
}
