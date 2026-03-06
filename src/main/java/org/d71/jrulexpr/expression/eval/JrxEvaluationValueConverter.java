package org.d71.jrulexpr.expression.eval;

import org.openhab.automation.jrule.rules.value.JRuleValue;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;

public class JrxEvaluationValueConverter implements EvaluationValueConverterIfc {
    private static JrxEvaluationValueConverter instance = null;

    public static synchronized JrxEvaluationValueConverter getInstance() {
        if (instance == null) {
            instance = new JrxEvaluationValueConverter();
        }
        return instance;
    }

    private final DefaultEvaluationValueConverter defaultConverter = new DefaultEvaluationValueConverter();

    /**
     * jrx expression: <item A> == "some string" || <item B> > 5 && ...
     *  
     *      -> EvalExpression.withValues(Map<itemName, JRuleValue>) 
     *      -> evaluate 
     *      -> (itemName=JRuleValue) 
     *      -> JRuleValue
     *      -> value (Object) 
     *      -> EvaluationValue
     * 
     */
    @Override
    public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
        Object valueObject = object;

        if (object instanceof JRuleValue) {
            valueObject =  JRuleValueHandler.extractJRuleValue((JRuleValue) object);
        }

        return defaultConverter.convertObject(valueObject, configuration);
    }

}
