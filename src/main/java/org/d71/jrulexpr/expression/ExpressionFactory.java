package org.d71.jrulexpr.expression;

import org.d71.jrulexpr.item.JrxItem;

public class ExpressionFactory {

    public static JrxExpression createJrxExpression(JrxItem item) {
        return (JrxExpression) createExpression(JRuleXprExpressionType.JRX, item);
    }

    public static JrxtExpression createJrxtExpression(JrxItem item) {
        return (JrxtExpression) createExpression(JRuleXprExpressionType.JRXT, item);
    }

    public static JrxfExpression createJrxfExpression(JrxItem item) {
        return (JrxfExpression) createExpression(JRuleXprExpressionType.JRXF, item);
    }

    public static JrxcExpression createJrxcExpression(JrxItem item) {
        return (JrxcExpression) createExpression(JRuleXprExpressionType.JRXC, item);
    }

    public static JrxpExpression createJrxpExpression(JrxItem item) {
        return (JrxpExpression) createExpression(JRuleXprExpressionType.JRXP, item);
    }

    public static JrxvExpression createJrxvExpression(JrxItem item, String name) {
        return (JrxvExpression) createExpression(JRuleXprExpressionType.JRXV, item, name);
    }

    public static JRuleXprExpression<?> createExpression(JRuleXprExpressionType expressionType, JrxItem item) {
        return createExpression(expressionType, item, null);
    }

    public static JRuleXprExpression<?> createExpression(JRuleXprExpressionType expressionType, JrxItem item, String name) {
        JRuleXprExpression<?> expression = null;

        switch (expressionType) {
            case JRXC:
                expression = new JrxcExpression(item);
                break;
            case JRXP:
                expression = new JrxpExpression(item);
                break;
            case JRX:
                expression = new JrxExpression(item);
                break;
            case JRXT:
                expression = new JrxtExpression(item);
                break;
            case JRXF:
                expression = new JrxfExpression(item);
                break;
            case JRXV:
                expression = new JrxvExpression(item, name);
                break;
        }
        return expression;
    }
}
