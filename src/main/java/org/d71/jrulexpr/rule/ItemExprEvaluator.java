package org.d71.jrulexpr.rule;

import java.util.List;

import org.openhab.automation.jrule.internal.handler.JRuleEventHandler;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;

public class ItemExprEvaluator {
    private ItemRegistry itemRegistry = null;

    private EvaluationValueConverterIfc valueConverter = new EvaluationValueConverterIfc() {
        private EvaluationValueConverterIfc defaultConverter = new DefaultEvaluationValueConverter();

        @Override
        public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
            EvaluationValue val = object instanceof DecimalType
                    ? (EvaluationValue.numberValue(((DecimalType) object).toBigDecimal()))
                    : defaultConverter.convertObject(object, configuration);
            return val;
        }
    };

    public ItemExprEvaluator(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public EvaluationValue eval(String itemName) throws Exception {
        String jrx = getJrx(itemRegistry.getItem(itemName));
        Expression ezyExpr = new Expression(jrx,
                ExpressionConfiguration.builder().evaluationValueConverter(valueConverter).build());

        List<Item> items = ezyExpr.getUndefinedVariables().stream().map(v -> itemRegistry.get(v)).toList();
        items.forEach(i -> ezyExpr.with(i.getName(), i.getState()));
        return ezyExpr.evaluate();
    }

    public String getJrx(Item item) {
        String jrx = item.getTags().stream().filter(t -> t.startsWith("jrx")).findFirst().orElse(null);
        return jrx.replaceFirst("jrx\s*=\s*", "");
    }    
}
