package org.d71.jrulexpr.expression;

import static org.d71.jrulexpr.expression.ItemExpressionType.*;

public class ItemExpressionFactory {
    public static IItemExpression getItemExpression(ItemExpressionType type, String itemName) {
        IItemExpression rv = null;
        if (type.equals(JRX)) rv = new JrxExpression(itemName);
        else if (type.equals(JRXT)) rv = new JrxtExpression(itemName);
        else if (type.equals(JRXF)) rv = new JrxfExpression(itemName);
        else if (type.equals(JRXP)) rv = new JrxpExpression(itemName);
        return rv;
    }
}
