package org.d71.jrulexpr.rule;

import java.util.Collections;
import java.util.Set;

public interface RuleTrigger {
    default String getCronExpression() {
        return null;
    }
    
    default Set<String> getGroups() {
        return Collections.emptySet();
    }
}
