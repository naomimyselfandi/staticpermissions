package io.github.naomimyselfandi.staticpermissions.core;

import java.lang.reflect.Method;

interface MethodRoleHelper {
    MethodRole getRole(Method method);
}
