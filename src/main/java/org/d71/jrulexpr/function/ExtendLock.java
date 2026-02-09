package org.d71.jrulexpr.function;

import java.time.Duration;

import com.ezylang.evalex.functions.FunctionParameter;

@FunctionParameter(name = "duration", isVarArg = true)
public class ExtendLock extends AbstractItemLockFunction {
    @Override
    public final String getToken() {
        return "ELOCK";
    }

    @Override
    public Boolean getValue(Object... parameters) {
        return extendLock(Duration.ofSeconds((int)parameters[0]));
    }
}
