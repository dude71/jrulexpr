package org.d71.jrulexpr.function;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JrxFunctionRegistry {
    private static JrxFunctionRegistry registry;

    public static synchronized JrxFunctionRegistry getInstance() {
        if (registry == null) {
            registry = new JrxFunctionRegistry();
            registry.register(ChannelFunction.class);
            registry.register(ExecFunction.class);
            registry.register(GroupFunction.class);
            registry.register(HostFunction.class);
            registry.register(HourFunction.class);
            registry.register(InitialValueFunction.class);
            registry.register(LockFunction.class);
            registry.register(MinTimeFunction.class);
            registry.register(MinuteFunction.class);
            registry.register(NowFunction.class);
            registry.register(SubStrFunction.class);
        }
        return registry;
    }

    private Map<String, Class<? extends JrxFunction<? extends Object>>> functions = new HashMap<>();

    private JrxFunctionRegistry() {
    }

    public synchronized void register(Class<? extends JrxFunction<? extends Object>> clazz) {
        functions.put(getInstance(clazz).getToken(), clazz);
    }

    public synchronized boolean isRegistered(String token) {
        return functions.keySet().contains(token);
    }

    public synchronized Set<Class<? extends JrxFunction<? extends Object>>> getFunctions() {
        return functions.values().stream().collect(Collectors.toSet());
    }

    public synchronized Set<String> getFunctionTokens() {
        return functions.keySet().stream().collect(Collectors.toSet());
    }

    public synchronized JrxFunction<?> getFunctionInstance(String token) {
        return getInstance(functions.get(token));
    }

    private JrxFunction<?> getInstance(Class<? extends JrxFunction<? extends Object>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
