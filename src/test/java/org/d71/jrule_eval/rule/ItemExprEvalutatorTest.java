package org.d71.jrule_eval.rule;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.d71.jrulexpr.rule.ItemExprEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;

import com.ezylang.evalex.data.EvaluationValue;

@ExtendWith(MockitoExtension.class)
public class ItemExprEvalutatorTest {
    @Mock
    private ItemRegistry itemRegistry;

    // SUT
    @InjectMocks
    private ItemExprEvaluator evaluator = new ItemExprEvaluator(itemRegistry);

    @Test
    public void evalTwoConditionsAnd() throws Exception {
        String name = "DM_LIVINGROOM_BULB";
        getMockedItem("NR_HOME_PROX_C", "1");
        getMockedItem("NR_KITCHEN_MOTION", "1");
        Item itm = getMockedItem(name, "1", "jrx=NR_HOME_PROX_C == 1 && NR_KITCHEN_MOTION == 1");
        EvaluationValue eval = evaluator.eval(name);
        assertTrue(eval.getBooleanValue());
    }

    private Item getMockedItem(String name, String value, String... tags) throws Exception {
        Item itm = Mockito.mock(Item.class);
        Mockito.lenient().when(itm.getName()).thenReturn(name);
        Mockito.lenient().when(itm.getState()).thenReturn(DecimalType.valueOf(value));
        Mockito.lenient().when(itm.getTags()).thenReturn(new HashSet<>(Arrays.asList(tags)));
        Mockito.lenient().when(itemRegistry.getItem(name)).thenReturn(itm);
        Mockito.lenient().when(itemRegistry.get(name)).thenReturn(itm);
        return itm;
    }
}
