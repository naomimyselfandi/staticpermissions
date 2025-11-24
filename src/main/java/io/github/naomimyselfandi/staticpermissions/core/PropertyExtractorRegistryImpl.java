package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
class PropertyExtractorRegistryImpl implements PropertyExtractorRegistry {

    private final List<PropertyExtractor<?>> propertyExtractors;
    private final ConversionService conversionService;
    private final NamingConvention namingConvention;

    PropertyExtractorRegistryImpl(
            List<PropertyExtractor<?>> propertyExtractors,
            ConversionService conversionService,
            NamingConvention namingConvention
    ) {
        this.propertyExtractors = propertyExtractors
                .stream()
                .sorted(Comparator.comparing(it -> -depth(it.getSupportedType())))
                .toList();
        this.conversionService = conversionService;
        this.namingConvention = namingConvention;
    }

    @Override
    public PropertyExtractor<?> get(Class<?> type) {
        return propertyExtractors
                .stream()
                .filter(extractor -> extractor.getSupportedType().isAssignableFrom(type))
                .findFirst()
                .orElseGet(() -> new ReflectivePropertyExtractor<>(conversionService, namingConvention, type));
    }

    private static int depth(Class<?> type) {
        return 1 + Stream
                .concat(Stream.ofNullable(type.getSuperclass()), Arrays.stream(type.getInterfaces()))
                .mapToInt(PropertyExtractorRegistryImpl::depth)
                .max()
                .orElse(0);
    }

}
