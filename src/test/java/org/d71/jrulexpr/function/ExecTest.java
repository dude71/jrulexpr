package org.d71.jrulexpr.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.d71.jrulexpr.AbstractItemTest;
import org.junit.jupiter.api.Test;

public class ExecTest extends AbstractItemTest {

    @Test
    public void execFunction() {
        Exec func = new Exec();
        assertEquals("/etc/passwd", func.getValue("ls /etc/passwd"));
    }
    
}
