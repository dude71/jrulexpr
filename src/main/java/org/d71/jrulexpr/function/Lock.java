package org.d71.jrulexpr.function;

import java.time.Duration;

import com.ezylang.evalex.functions.FunctionParameter;

@FunctionParameter(name = "duration", isVarArg = true)
public class Lock extends AbstractItemLockFunction {
    @Override
    public final String getToken() {
        return "LOCK";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        return lock(Duration.ofSeconds((int)parameters[0]));
    }
}
