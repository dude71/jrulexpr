package org.d71.jrulexpr.expression;

import java.util.Optional;
import java.util.Set;

import org.d71.jrulexpr.function.JrxFunction;
import org.d71.jrulexpr.item.JrxItem;

public interface JRuleXprExpression<T> {
    JRuleXprExpressionType getJrxType();

    default Optional<String> getDefinition() {
        JRuleXprExpressionType type = getJrxType();
        JrxItem item = getContainerItem();
        return switch (type) {
            case JRX -> item.getJrx();
            case JRXT -> item.getJrxt();
            case JRXF -> item.getJrxf();
            case JRXC -> item.getJrxc();
            case JRXP -> item.getJrxp();
            default -> Optional.empty();
        };
    }

    JrxItem getContainerItem();

    default Set<JrxItem> getReferencedItems() {
        return getReferencedItems(false );
    }

    Set<JrxItem> getReferencedItems(boolean includeJrxvItems );

    default Set<JrxFunction<?>> getReferencedFunctions() {
        return getReferencedFunctions(false);
    }

    Set<JrxFunction<?>> getReferencedFunctions(boolean includeJrxvFunctions );

    T evaluate();
}
