package io.github.naomimyselfandi.staticpermissions.core;

import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EqualsAndHashCode
class IntentInvocationHandler<I> implements Supplier<I>, InvocationHandler {

    private static final ClassLoader CLASS_LOADER = IntentInvocationHandler.class.getClassLoader();

    private final Class<I> type;
    private final Map<String, Object> values;
    private final Authentication authentication;
    private final NamingConvention namingConvention;
    private final MethodRoleHelper methodRoleHelper;

    IntentInvocationHandler(
            Class<I> type,
            Map<String, Object> values,
            Authentication authentication,
            NamingConvention namingConvention,
            MethodRoleHelper methodRoleHelper
    ) {
        this.type = type;
        this.values = Map.copyOf(values);
        this.authentication = authentication;
        this.namingConvention = namingConvention;
        this.methodRoleHelper = methodRoleHelper;
    }

    @Override
    public I get() {
        @SuppressWarnings("unchecked")
        var proxy = (I) Proxy.newProxyInstance(CLASS_LOADER, new Class[]{type}, this);
        return proxy;
    }

    @Override
    public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return switch (methodRoleHelper.getRole(method)) {
            case EQUALS -> invokeEquals(args[0]);
            case HASH_CODE -> hashCode();
            case TO_STRING -> toString();
            case AUTHENTICATION -> authentication;
            case DATA_MAP -> values;
            case REQUIRED_PROPERTY, OPTIONAL_PROPERTY -> invokeGetter(proxy, method, args);
            case NON_PROPERTY -> invokeDefault(proxy, method, args);
        };
    }

    private boolean invokeEquals(@Nullable Object other) {
        return other != null && Proxy.isProxyClass(other.getClass()) && equals(Proxy.getInvocationHandler(other));
    }

    private Object invokeGetter(Object proxy, Method method, Object[] args) throws Throwable {
        var name = method.getName();
        return values.containsKey(name) ? values.get(name) : invokeDefault(proxy, method, args);
    }

    private static Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return type.getSimpleName() + values
                .entrySet()
                .stream()
                .map(it -> "%s=%s".formatted(namingConvention.normalize(it.getKey()), it.getValue()))
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
