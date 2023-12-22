package org.d71.jrulexpr.rule.functions;

import com.ezylang.evalex.functions.AbstractFunction;
import org.openhab.core.items.Item;

public abstract class AbstractItemFunction extends AbstractFunction {
    protected Item item;
    protected AbstractItemFunction(Item item) {
        this.item = item;
    }
}
