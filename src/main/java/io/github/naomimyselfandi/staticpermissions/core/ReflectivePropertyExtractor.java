package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

class ReflectivePropertyExtractor<T> implements PropertyExtractor<T> {

    private final ConcurrentLruCache<String, Optional<Method>> cache;

    final ConversionService conversionService;
    final NamingConvention namingConvention;
    final Class<T> source;

    ReflectivePropertyExtractor(
            ConversionService conversionService,
            NamingConvention namingConvention,
            Class<T> source
    ) {
        this.conversionService = conversionService;
        this.namingConvention = namingConvention;
        this.source = source;
        cache = new ConcurrentLruCache<>(32, propertyName -> Arrays
                .stream(source.getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> namingConvention.normalize(method.getName()).equals(propertyName))
                .min(Comparator.comparing(Method::getName)));
    }

    @Override
    public ValidationResult validate(String propertyName, TypeDescriptor propertyType) {
        var method = cache.get(propertyName);
        if (method.isEmpty()) {
            return ValidationResult.UNAVAILABLE;
        } else if (!conversionService.canConvert(getReturnType(method.get()), propertyType)) {
            return ValidationResult.TYPE_MISMATCH;
        } else {
            return ValidationResult.OK;
        }
    }

    @Override
    public @Nullable Object extract(Object source, Method propertyMethod, String propertyName) {
        var sourceMethod = cache.get(propertyName).orElse(null);
        if (sourceMethod == null) {
            return null;
        }
        var propertyType = getReturnType(propertyMethod);
        var sourceType = getReturnType(sourceMethod);
        if (conversionService.canConvert(sourceType, propertyType)) {
            ReflectionUtils.makeAccessible(sourceMethod);
            var value = ReflectionUtils.invokeMethod(sourceMethod, source);
            return conversionService.convert(value, sourceType, propertyType);
        } else {
            return TYPE_MISMATCH;
        }
    }

    private static TypeDescriptor getReturnType(Method method) {
        return new TypeDescriptor(ResolvableType.forMethodReturnType(method), null, null);
    }

}
