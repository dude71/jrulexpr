package org.d71.jrulexpr.expression;

import java.util.Set;

import org.openhab.core.items.Item;

import com.ezylang.evalex.data.EvaluationValue;

public interface IItemExpression {
    Set<Item> getXprItems() throws Exception;

    Set<String> getUdFunctions() throws Exception;
    
    EvaluationValue evaluate() throws Exception;
}
