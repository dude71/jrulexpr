package org.d71.jrulexpr.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.d71.jrulexpr.AbstractItemTest;
import org.junit.jupiter.api.Test;

public class ExecFunctionTest extends AbstractItemTest {

    @Test
    public void execFunction() {
        ExecFunction func = new ExecFunction();
        assertEquals("/etc/passwd", func.getValue("ls /etc/passwd"));
    }
    
}
