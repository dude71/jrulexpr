package org.d71.jrulexpr.expression;

import com.ezylang.evalex.data.EvaluationValue;

public class JrxExpression extends AbstractItemExpression {
    JrxExpression(String itemName) {
        super(itemName, "jrx");
    }

    @Override
    public EvaluationValue evaluate() throws Exception { // evaluate jrx to (new) item state
        String jrx = getXpr().orElseThrow(() -> new IllegalStateException("jrx tag must be present!"));

        EvaluationValue ev = evalXpr(jrx);
        if (!ev.isBooleanValue()) {
            throw new IllegalStateException("jrx must evaluate to boolean!");
        }
        LOGGER.debug("jrx: " + jrx + " ev: " + ev);

        return ev;
    }
}
