package org.d71.jrulexpr.function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotNullTest {

    @Test
    void getValue_returnsTrueWhenAllArgumentsAreNonNull() {
        NotNull notNull = new NotNull();

        assertTrue(notNull.getValue("value", 42, true));
    }

    @Test
    void getValue_returnsFalseWhenAnyArgumentIsNull() {
        NotNull notNull = new NotNull();

        assertFalse(notNull.getValue("value", null, true));
    }

    @Test
    void getToken_returnsExpectedToken() {
        NotNull notNull = new NotNull();

        assertTrue(notNull.getToken().equals("NN"));
    }
}
