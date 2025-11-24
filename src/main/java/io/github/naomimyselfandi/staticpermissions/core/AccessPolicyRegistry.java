package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;

import java.util.List;

interface AccessPolicyRegistry {
    List<? extends AccessPolicy<?>> get(Class<?> type);
}
