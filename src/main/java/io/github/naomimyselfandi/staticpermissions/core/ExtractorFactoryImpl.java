package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import io.github.naomimyselfandi.staticpermissions.core.PolyExtractor.Property;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentLruCache;

import java.lang.reflect.Method;
import java.util.*;

@Component
class ExtractorFactoryImpl implements ExtractorFactory {

    private final NamingConvention namingConvention;
    private final ConversionService conversionService;
    private final MethodRoleHelper methodRoleHelper;
    private final ConcurrentLruCache<Class<?>, PropertyExtractor<?>> propertyExtractorRegistry;

    ExtractorFactoryImpl(
            NamingConvention namingConvention,
            ConversionService conversionService,
            MethodRoleHelper methodRoleHelper,
            PropertyExtractorRegistry propertyExtractorRegistry
    ) {
        this.namingConvention = namingConvention;
        this.conversionService = conversionService;
        this.methodRoleHelper = methodRoleHelper;
        this.propertyExtractorRegistry = new ConcurrentLruCache<>(256, propertyExtractorRegistry::get);
    }

    @Override
    public Optional<Extractor<?>> apply(Class<?> source, Class<?> target) {
        var properties = scan(target);
        var requiredProperties = properties.stream().filter(property -> !property.optional()).toList();
        if (requiredProperties.size() == 1) {
            var property = requiredProperties.get(0);
            var method = property.method();
            var returnType = getReturnType(method);
            if (conversionService.canConvert(TypeDescriptor.valueOf(source), returnType)) {
                return Optional.of(new MonoExtractor<>(method.getName(), returnType, conversionService));
            }
        }
        @SuppressWarnings("unchecked")
        var propertyExtractor = (PropertyExtractor<Object>) propertyExtractorRegistry.get(source);
        if (validate(propertyExtractor, properties)) {
            return Optional.of(new PolyExtractor<>(properties, propertyExtractor));
        } else {
            return Optional.empty();
        }
    }

    private boolean validate(PropertyExtractor<?> extractor, List<Property> properties) {
        return properties.stream().allMatch(property -> {
            var returnType = getReturnType(property.method());
            return switch (extractor.validate(property.name(), returnType)) {
                case OK -> true;
                case UNAVAILABLE -> property.optional();
                case TYPE_MISMATCH -> false;
            };
        });
    }

    private List<Property> scan(Class<?> target) {
        return Arrays
                .stream(target.getMethods())
                .sorted(Comparator.comparing(Method::getName))
                .map(method -> switch (methodRoleHelper.getRole(method)) {
                    case EQUALS, HASH_CODE, TO_STRING, AUTHENTICATION, DATA_MAP, NON_PROPERTY -> null;
                    case REQUIRED_PROPERTY -> {
                        var key = method.getName();
                        var name = namingConvention.normalize(key);
                        yield new Property(name, method, false);
                    }
                    case OPTIONAL_PROPERTY -> {
                        var key = method.getName();
                        var name = namingConvention.normalize(key);
                        yield new Property(name, method, true);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static TypeDescriptor getReturnType(Method method) {
        return new TypeDescriptor(ResolvableType.forMethodReturnType(method), null, null);
    }

}
