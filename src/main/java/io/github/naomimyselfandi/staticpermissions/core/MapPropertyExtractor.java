package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
@RequiredArgsConstructor
final class MapPropertyExtractor implements PropertyExtractor<Map<?, ?>> {

    private final ConversionService conversionService;

    @Override
    public @Nullable Object extract(Map<?, ?> source, Method method, String propertyName) {
        if (source.containsKey(propertyName)) {
            var value = source.get(propertyName);
            var returnType = returnType(method);
            if (conversionService.canConvert(TypeDescriptor.forObject(value), returnType)) {
                return conversionService.convert(value, returnType);
            } else {
                return TYPE_MISMATCH;
            }
        } else {
            return null;
        }
    }

    private static TypeDescriptor returnType(Method method) {
        return new TypeDescriptor(ResolvableType.forMethodReturnType(method), null, null);
    }

}
