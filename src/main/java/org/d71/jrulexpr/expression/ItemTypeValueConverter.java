package org.d71.jrulexpr.expression;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;

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
        EvaluationValue val = null;

        if (object instanceof DecimalType)
            val = EvaluationValue.numberValue(
                    ((DecimalType) object).toBigDecimal());
        else if (object instanceof OnOffType)
            val = EvaluationValue.stringValue(
                    ((OnOffType) object).toString());
        else if (object instanceof UnDefType) {
            val = EvaluationValue.nullValue();
        } else
            val = defaultConverter.convertObject(object, configuration);

        return val;
    }
    
}
