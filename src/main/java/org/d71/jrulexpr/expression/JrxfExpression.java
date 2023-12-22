package org.d71.jrulexpr.expression;

import java.math.BigDecimal;
import java.util.Optional;

import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;

import com.ezylang.evalex.data.EvaluationValue;

public class JrxfExpression extends AbstractItemExpression {
    JrxfExpression(String itemName) {
        super(itemName, "jrxf");
    }

    protected EvaluationValue getDefault() {
        Item item = getItem();
        EvaluationValue ev = EvaluationValue.nullValue();
        if (CoreItemFactory.DIMMER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(0));
        } else if (CoreItemFactory.NUMBER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(0));
        } else if (CoreItemFactory.SWITCH.equals(item.getType())) {
            ev = EvaluationValue.stringValue("OFF");
        }
        return ev;
    }
}
