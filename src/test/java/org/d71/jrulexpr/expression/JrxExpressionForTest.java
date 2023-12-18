package org.d71.jrulexpr.expression;

import org.openhab.core.items.ItemRegistry;

public class JrxExpressionForTest extends JrxExpression {
    JrxExpressionForTest(String itemName, ItemRegistry itemRegistry) {
        super(itemName);
        super.itemRegistry = itemRegistry;
    }

}
