package org.d71.jrulexpr.rule;

import java.util.Collections;
import java.util.Set;

public interface RuleTrigger {
    default String getCronExpression() {
        return null;
    }
    default Set<String> getGroupNames() {
        return Collections.emptySet();
    }

    default String getItemName() { return null; }

    default boolean evaluateOnUpdate() {
        return false;
    }

    default boolean evaluateOnChange() {
        return false;
    }

    default String getChannel() {
        return null;
    }
}
