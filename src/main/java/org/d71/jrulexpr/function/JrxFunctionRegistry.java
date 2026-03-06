package org.d71.jrulexpr.function;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JrxFunctionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(JrxFunctionRegistry.class);    

    private static JrxFunctionRegistry registry;

    public static synchronized JrxFunctionRegistry getInstance() {
        if (registry == null) {
            LOGGER.debug("Getting instance of JrxFunctionRegistry and registering functions...");
            registry = new JrxFunctionRegistry();
            registry.register(ChangeAge.class);
            registry.register(Channel.class);
            registry.register(ExtendLock.class);
            registry.register(Enabled.class);
            registry.register(Exec.class);
            registry.register(Group.class);
            registry.register(Host.class);
            registry.register(Hour.class);
            registry.register(InitialValue.class);
            registry.register(LastChange.class);
            registry.register(Lock.class);
            registry.register(MinTime.class);
            registry.register(Minute.class);
            registry.register(Month.class);
            registry.register(NotNull.class);
            registry.register(Now.class);
            registry.register(PreviousValue.class);
            registry.register(SelfTriggered.class);
            registry.register(Sleep.class);
            registry.register(SubStr.class);
            registry.register(TagVal.class);
            registry.register(TriggeredBy.class);
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
        return functions.containsKey(token);
    }

    public synchronized Set<Class<? extends JrxFunction<? extends Object>>> getFunctions() {
        return new HashSet<>(functions.values());
    }

    public synchronized Set<String> getFunctionTokens() {
        return new HashSet<>(functions.keySet());
    }

    public synchronized JrxFunction<?> getFunctionInstance(String token) {
        LOGGER.debug("Getting function instance for token: " + token);
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
