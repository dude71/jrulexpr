package org.d71.jrulexpr.function;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ezylang.evalex.config.ExpressionConfiguration;

import org.d71.jrulexpr.expression.eval.JrxEvaluationValueConverter;
import org.d71.jrulexpr.item.JrxItem;
import org.d71.jrulexpr.item.JrxItemRegistry;
import org.d71.jrulexpr.rule.RuleTrigger;

public interface JrxFunction<V> {
    String getToken();

    default Set<RuleTrigger> getRuleTriggers() {
        return Collections.emptySet();
    }

    default void setItem(JrxItem item) {    
    }

    default void setItemRegistry(JrxItemRegistry registry) {
    }

    default void setParameters(List<Object> values) {
    }

    default ExpressionConfiguration getExpressionConfig() {
       return ExpressionConfiguration.builder().evaluationValueConverter(JrxEvaluationValueConverter.getInstance()).build();
    }

    V getValue(Object... parameters);
}