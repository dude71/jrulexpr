package org.d71.jrulexpr.expression;

import com.ezylang.evalex.data.EvaluationValue;

import java.util.Optional;

public class JrxpExpression extends AbstractItemExpression {

    JrxpExpression(String itemName) {
        super(itemName, "jrxp");
    }

    @Override
    public EvaluationValue evaluate() throws Exception {
        Optional<String> jrxp = getTagValue(tagName);
        return jrxp.isPresent() ? evalXpr(jrxp.get()) : EvaluationValue.booleanValue(true);
    }
    
}
