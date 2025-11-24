package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentLruCache;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
class StaticPermissionServiceImpl implements StaticPermissionService {

    private record Pair(Class<?> s, Class<?> t) {}

    private final NamingConvention namingConvention;
    private final MethodRoleHelper methodRoleHelper;
    private final ConcurrentLruCache<Pair, Optional<Extractor<?>>> extractorFactory;
    private final ConcurrentLruCache<Class<?>, List<? extends AccessPolicy<?>>> accessPolicyRegistry;
    private final ObjectProvider<ConfigurableConversionService> conversionServices;

    StaticPermissionServiceImpl(
            NamingConvention namingConvention,
            MethodRoleHelper methodRoleHelper,
            ExtractorFactory extractorFactory,
            AccessPolicyRegistry accessPolicyRegistry,
            ObjectProvider<ConfigurableConversionService> conversionServices
    ) {
        this.namingConvention = namingConvention;
        this.methodRoleHelper = methodRoleHelper;
        this.extractorFactory = new ConcurrentLruCache<>(256, it -> extractorFactory.apply(it.s, it.t));
        this.accessPolicyRegistry = new ConcurrentLruCache<>(256, accessPolicyRegistry::get);
        this.conversionServices = conversionServices;
    }

    @PostConstruct
    void initialize() {
        for (var conversionService : conversionServices) {
            conversionService.addConverter(new IntentConverter(this));
        }
    }

    @Override
    public boolean isSourceFor(Class<?> source, Class<?> type) {
        return type.isInterface()
                && Intent.class.isAssignableFrom(type)
                && extractorFactory.get(new Pair(source, type)).isPresent();
    }

    @Override
    public String normalizeMethodName(Method method) {
        return namingConvention.normalize(method.getName());
    }

    @Override
    public <I extends Intent> I require(Object source, Class<I> type) {
        var intent = createIntent(source, type);
        getDenial(intent, type).ifPresent(denial -> {throw denial.get();});
        return intent;
    }

    @Override
    public <I extends Intent> Optional<I> request(Object source, Class<I> type) {
        var intent = createIntent(source, type);
        var permitted = getDenial(intent, type).isEmpty();
        return Optional.ofNullable(permitted ? intent : null);
    }

    @SuppressWarnings("unchecked")
    private <I> I createIntent(Object source, Class<I> type) {
        return createIntent(source, (Class<Object>) source.getClass(), type);
    }

    private <S, I> I createIntent(S source, Class<S> sourceType, Class<I> type) {
        @SuppressWarnings("unchecked")
        var extractor = (Extractor<S>) extractorFactory.get(new Pair(sourceType, type)).orElseThrow(() -> {
            var message = "%s is not a valid source for %s.".formatted(sourceType, type);
            return new IllegalArgumentException(message);
        });
        var values = extractor.extract(source);
        var user = SecurityContextHolder.getContext().getAuthentication();
        return new IntentInvocationHandler<>(type, values, user, namingConvention, methodRoleHelper).get();
    }

    @SuppressWarnings("unchecked")
    private <I> Optional<AccessPolicy.Denial> getDenial(I intent, Class<I> type) {
        return accessPolicyRegistry
                .get(type)
                .stream()
                .map(it -> ((AccessPolicy<? super I>) it).apply(intent))
                .filter(Objects::nonNull)
                .findFirst();
    }

}
