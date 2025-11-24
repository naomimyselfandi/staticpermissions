package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
class AccessPolicyRegistryImpl implements AccessPolicyRegistry {

    private final List<AccessPolicy<?>> policies;

    AccessPolicyRegistryImpl(List<AccessPolicy<?>> accessPolicies) {
        this.policies = accessPolicies
                .stream()
                .sorted(Comparator.comparing(it -> depth(it.getIntentType())))
                .toList();
    }

    @Override
    public List<? extends AccessPolicy<?>> get(Class<?> type) {
        return policies
                .stream()
                .filter(it -> it.getIntentType().isAssignableFrom(type))
                .toList();
    }

    private static int depth(Class<?> type) {
        return 1 + Stream
                .concat(Stream.ofNullable(type.getSuperclass()), Arrays.stream(type.getInterfaces()))
                .mapToInt(AccessPolicyRegistryImpl::depth)
                .max()
                .orElse(0);
    }

}
