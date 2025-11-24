package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
class MethodRoleHelperImpl implements MethodRoleHelper {

    private static final Pattern RESERVED = Pattern.compile("__.*__");

    @Override
    public MethodRole getRole(Method method) {
        if (ReflectionUtils.isEqualsMethod(method)) {
            return MethodRole.EQUALS;
        } else if (ReflectionUtils.isHashCodeMethod(method)) {
            return MethodRole.HASH_CODE;
        } else if (ReflectionUtils.isToStringMethod(method)) {
            return MethodRole.TO_STRING;
        } else if (RESERVED.matcher(method.getName()).matches()) {
            return applyToReservedMethod(method);
        } else if (isProperty(method)) {
            return applyToProperty(method);
        } else if (Modifier.isAbstract(method.getModifiers())) {
            throw invalid(method);
        } else {
            return MethodRole.NON_PROPERTY;
        }
    }

    private static MethodRole applyToReservedMethod(Method method) {
        if (method.getParameterCount() == 0) {
            switch (method.getName()) {
                case "__auth__": return MethodRole.AUTHENTICATION;
                case "__data__": return MethodRole.DATA_MAP;
            }
        }
        throw invalid(method);
    }

    private static boolean isProperty(Method method) {
        return method.getParameterCount() == 0
                && !method.isAnnotationPresent(Intent.NotProperty.class)
                && method.getReturnType() != void.class;
    }

    private static MethodRole applyToProperty(Method method) {
        if (method.isDefault() || method.getReturnType() == Optional.class) {
            return MethodRole.OPTIONAL_PROPERTY;
        } else {
            return MethodRole.REQUIRED_PROPERTY;
        }
    }

    private static IllegalStateException invalid(Method method) {
        return new IllegalStateException("Invalid intent type method '%s'.".formatted(method));
    }

}
