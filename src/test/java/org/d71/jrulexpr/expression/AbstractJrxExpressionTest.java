package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.AbstractItemTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.CoreItemFactory;

@ExtendWith(MockitoExtension.class)

public abstract class AbstractJrxExpressionTest extends AbstractItemTest {
    @BeforeEach
    public void init() {
        createMockedItem("nItem1", CoreItemFactory.NUMBER, "10");
        createMockedItem("nItem2", CoreItemFactory.NUMBER, "20");
        createMockedItem("sItem1", CoreItemFactory.STRING, "item1");
        createMockedItem("sItem2", CoreItemFactory.STRING, "item2");
    }

    JrxExpression createJrxExpression(String xpr) {
        return new JrxExpression(xpr, itemRegistry, functionRegistry);
    }    
}
