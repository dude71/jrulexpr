package org.d71.jrulexpr.expression;

import org.openhab.core.items.ItemRegistry;

public class JrxpExpressionForTest extends JrxpExpression {
    JrxpExpressionForTest(String itemName, ItemRegistry itemRegistry) {
        super(itemName);
        super.itemRegistry = itemRegistry;
    }
}
