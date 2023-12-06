package org.d71.jrulexpr;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;

public class JRuleXprTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JRuleXprTest.class);

    private static EvaluationValueConverterIfc defaultConverter = new DefaultEvaluationValueConverter();

    public static void main(String[] args) throws Exception {
        // JRuleXpr.getInstance().generateItemRules();
        // JRuleXpr.getInstance().unload();

        Map<String, Object> convVals = new HashMap<>();

        String expr = "NR_HOME_PROX_C == 1 && NR_KITCHEN_MOTION == 1";

        Expression expression = new Expression(expr, ExpressionConfiguration.builder()
            .evaluationValueConverter(new EvaluationValueConverterIfc() {

                @Override
                public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
                    EvaluationValue val = object instanceof DecimalType ? 
                        (EvaluationValue.numberValue(((DecimalType)object).toBigDecimal())) : 
                        defaultConverter.convertObject(object, configuration);
                    return val;
                }
                
            })
            .build());

        // Expression expression = new Expression(expr);

        // expression.getAllASTNodes().forEach(n -> {
        //     if (n.getParameters().size() == 2) {
        //         LOGGER.warn(n.toJSON());
        //         Token t1 = n.getParameters().get(0).getToken();
        //         Token t2 = n.getParameters().get(1).getToken();

        //         if (t1.getType().equals(TokenType.VARIABLE_OR_CONSTANT) &&
        //             t2.getType().equals(TokenType.NUMBER_LITERAL)) {                
        //             LOGGER.warn("" + t1.getValue());
        //             (new BigDecimal(t2.getValue())).precision();
        //             convVals.put(t1.getValue(), DecimalType.valueOf("1"));
        //         }
        //     }
        // });

        expression.with("NR_HOME_PROX_C",  DecimalType.valueOf("1"));
        expression.with("NR_KITCHEN_MOTION", DecimalType.valueOf("1"));
        
        LOGGER.warn("eval: " + expression.evaluate().getBooleanValue());

    }    
}
