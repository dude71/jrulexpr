package org.d71.jrulexpr.expression;

import java.math.BigDecimal;

import org.d71.jrulexpr.item.ValueConverter;
import org.openhab.automation.jrule.rules.value.JRuleValue;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;

public class ItemTypeValueConverter implements EvaluationValueConverterIfc {
    private static ItemTypeValueConverter instance;

    public static final synchronized ItemTypeValueConverter getInstance() {
        if (instance == null) {
            instance = new ItemTypeValueConverter();
        }
        return instance;
    }

    private EvaluationValueConverterIfc defaultConverter = new DefaultEvaluationValueConverter();

    @Override
    public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
        Object convertedObj = object instanceof JRuleValue ? ValueConverter.convertToObject((JRuleValue) object)
                : defaultConverter.convertObject(object, configuration);
        EvaluationValue val;

        if (convertedObj instanceof EvaluationValue)
            val = (EvaluationValue)convertedObj;
        else if (convertedObj instanceof BigDecimal)
            val = EvaluationValue.numberValue((BigDecimal) convertedObj);
        else if (convertedObj instanceof String)
            val = EvaluationValue.stringValue((String) convertedObj);
        else {
            throw new RuntimeException("Object " + object + " cannot be converted!");
        }

        return val;
    }

}
