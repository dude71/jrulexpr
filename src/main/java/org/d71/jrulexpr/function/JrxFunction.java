package org.d71.jrulexpr.function;

import org.d71.jrulexpr.item.JrxItem;

public interface JrxFunction<V> {
    String getToken();

    default String getCronExpression() {
        return null;
    }

    default void setItem(JrxItem item) {    
    }

    V getValue(Object... parameters);
}
