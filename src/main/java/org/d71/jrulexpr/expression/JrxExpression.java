package org.d71.jrulexpr.expression;

import com.ezylang.evalex.data.EvaluationValue;

public class JrxExpression extends AbstractItemExpression {
    JrxExpression(String itemName) {
        super(itemName, "jrx");
    }

    @Override
    protected EvaluationValue getDefault() {
        throw new IllegalStateException("jrx tag must be present!");
    }

    @Override
    protected void validateXprValue(EvaluationValue value) {
        if (!value.isBooleanValue()) {
            throw new IllegalStateException("jrx must evaluate to boolean!");
        }
    }
}
