package io.github.naomimyselfandi.staticpermissions.core;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode
@RequiredArgsConstructor
class MonoExtractor<S> implements Extractor<S> {

    private final String name;
    private final TypeDescriptor targetType;
    private final ConversionService conversionService;

    @Override
    public Map<String, Object> extract(S source) {
        return Collections.singletonMap(name, conversionService.convert(source, targetType));
    }

}
