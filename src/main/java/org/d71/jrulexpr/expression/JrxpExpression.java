package org.d71.jrulexpr.expression;

import com.ezylang.evalex.data.EvaluationValue;

public class JrxpExpression extends AbstractItemExpression {
    JrxpExpression(String itemName) {
        super(itemName, "jrxp");
    }

    @Override
    protected EvaluationValue getDefault() {
        // no jrxp means ok/continue
        return EvaluationValue.booleanValue(true);
    }
}
