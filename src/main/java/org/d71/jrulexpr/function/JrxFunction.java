package org.d71.jrulexpr.function;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.rule.RuleTrigger;

public interface JrxFunction<V> {
    String getToken();

    default Optional<RuleTrigger> getRuleTrigger() {
        return Optional.empty();
    }

    default void setItem(JrxItem item) {    
    }

    default void setParameters(List<Object> values) {
    }

    V getValue(Object... parameters);
}
