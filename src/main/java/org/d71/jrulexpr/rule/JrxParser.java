package org.d71.jrulexpr.rule;

import java.util.Optional;

import org.openhab.core.items.Item;

public class JrxParser {
    private static Optional<String> getTagValue(Item item, String tagName) {
        Optional<String> tagVal = item.getTags().stream()
            .filter(t -> t.matches("^" + tagName + "\s*=.*$"))
            .findFirst();
        return tagVal.isPresent() ? Optional.of(tagVal.get().replaceFirst(tagName + "\s*=", "")) : tagVal;
    }

    public static Optional<String> getJrx(Item item) {
        return getTagValue(item, "jrx");
    }
    
    public static Optional<String> getJrxt(Item item) {
        return getTagValue(item, "jrxt");
    }

    public static Optional<String> getJrxf(Item item) {
        return getTagValue(item, "jrxf");
    }    
}
