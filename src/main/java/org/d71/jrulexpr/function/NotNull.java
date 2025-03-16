package org.d71.jrulexpr.function;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@FunctionParameter(name = "itemName")
@FunctionParameter(name = "itemName2")
@FunctionParameter(name = "itemName3", isVarArg = true)
public class NotNull extends AbstractFunction implements JrxFunction<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotNull.class);
    private JrxItemRegistry itemRegistry;

    @Override
    public void setItemRegistry(JrxItemRegistry registry) {
        this.itemRegistry = registry;
    }

    @Override
    public String getToken() {
        return "NN";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        boolean rv = false;
        for (Object p : parameters) {
            Optional<JrxItem> jrxItem = itemRegistry.get((String) p);
            if (jrxItem.isEmpty()) throw new RuntimeException("Cannot find item " + (String)p + " !");
            rv = jrxItem.get().getState() == null;
            if (!rv) {
                LOGGER.debug("item " + jrxItem.get().getName() + " is null");
                break;
            }
        }
        return rv;
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token token, EvaluationValue... parameters) throws EvaluationException {
        Object[] itemNames = Arrays.stream(parameters).map(EvaluationValue::getStringValue).toList().toArray(Object[]::new);
        return EvaluationValue.booleanValue(getValue(itemNames));
    }

}
