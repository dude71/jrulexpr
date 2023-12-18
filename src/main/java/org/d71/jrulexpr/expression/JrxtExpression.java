package org.d71.jrulexpr.expression;

import java.math.BigDecimal;
import java.util.Optional;

import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;

import com.ezylang.evalex.data.EvaluationValue;

public class JrxtExpression extends AbstractItemExpression {
    JrxtExpression(String itemName) {
        super(itemName, "jrxt");
    }

    @Override
    public EvaluationValue evaluate() throws Exception {
        Optional<String> jrxt = getTagValue(tagName);
        return jrxt.isPresent() ? evalXpr(jrxt.get()) : getDefault();
    }

    EvaluationValue getDefault() {
        Item item = getItem();
        EvaluationValue ev = EvaluationValue.nullValue();
        if (CoreItemFactory.DIMMER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(90));
        } else if (CoreItemFactory.NUMBER.equals(item.getType())) {
            ev = EvaluationValue.numberValue(BigDecimal.valueOf(1));
        } else if (CoreItemFactory.SWITCH.equals(item.getType())) {
            ev = EvaluationValue.stringValue("ON");
        }
        return ev;
    }
}
