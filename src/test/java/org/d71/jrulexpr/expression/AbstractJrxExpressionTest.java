package org.d71.jrulexpr.expression;

import java.util.Arrays;
import java.util.HashSet;

import org.d71.jrulexpr.AbstractItemTest;
import org.d71.jrulexpr.function.JrxFunctionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

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
