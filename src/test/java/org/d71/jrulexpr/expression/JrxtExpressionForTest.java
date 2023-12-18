package org.d71.jrulexpr.expression;

import org.openhab.core.items.ItemRegistry;

public class JrxtExpressionForTest extends JrxtExpression {
    JrxtExpressionForTest(String itemName, ItemRegistry itemRegistry) {
        super(itemName);
        super.itemRegistry = itemRegistry;
    }    
}
