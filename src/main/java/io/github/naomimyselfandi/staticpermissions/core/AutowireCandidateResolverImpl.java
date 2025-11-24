package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.IntentFactory;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
class AutowireCandidateResolverImpl implements AutowireCandidateResolver {

    final AutowireCandidateResolver delegate;
    final BeanFactory beanFactory;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final StaticPermissionService staticPermissionService = beanFactory.getBean(StaticPermissionService.class);

    @Override
    public @Nullable Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        var result = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        if (result == null) {
            var type = descriptor.getResolvableType();
            if (type.toClass() == IntentFactory.class) {
                var generics = type.getGenerics();
                var sourceType = generics[0].toClass();
                var intentType = generics[1].toClass().asSubclass(Intent.class);
                var service = getStaticPermissionService();
                if (service.isSourceFor(sourceType, intentType)) {
                    result = new IntentFactoryImpl<>(intentType, service);
                }
            }
        }
        return result;
    }

}
