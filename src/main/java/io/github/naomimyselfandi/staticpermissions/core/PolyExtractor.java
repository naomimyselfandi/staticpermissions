package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.*;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class PolyExtractor<S> implements Extractor<S> {

    record Property(String name, Method method, boolean optional) {}

    private final List<Property> properties;
    private final PropertyExtractor<? super S> extractor;

    @Override
    public Map<String, Object> extract(S source) {
        var result = new HashMap<String, Object>();
        for (var property : properties) {
            var extracted = extractor.extract(source, property.method, property.name);
            if (extracted == PropertyExtractor.TYPE_MISMATCH) {
                var message = "Value for '%s' in %s is not the correct type.".formatted(property.name, source);
                throw new NoSuchElementException(message);
            } else if (extracted != null) {
                result.put(property.method.getName(), extracted);
            } else if (!property.optional()) {
                var message = "Couldn't find a value for '%s' in %s.".formatted(property.name, source);
                throw new NoSuchElementException(message);
            }
        }
        return Collections.unmodifiableMap(result);
    }

}
