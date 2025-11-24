package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapPropertyExtractorTest {

    private interface Source {}
    private interface Target {}

    @Mock
    private Source source;

    @Mock
    private Target target;

    private Method method;
    private TypeDescriptor sourceType, targetType;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private MapPropertyExtractor fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        interface Holder {
            Target someMethod();
        }
        method = Holder.class.getMethod("someMethod");
        sourceType = TypeDescriptor.forObject(source);
        targetType = TypeDescriptor.valueOf(Target.class);
    }

    @Test
    void extract() {
        var propertyName = UUID.randomUUID().toString();
        var map = Map.of(propertyName, source);
        when(conversionService.canConvert(sourceType, targetType)).thenReturn(true);
        when(conversionService.convert(source, targetType)).thenReturn(target);
        assertThat(fixture.extract(map, method, propertyName)).isEqualTo(target);
    }

    @Test
    void extract_WhenTheValueIsAbsent_ThenNull() {
        var propertyName = UUID.randomUUID().toString();
        var map = Map.of(UUID.randomUUID().toString(), target);
        assertThat(fixture.extract(map, method, propertyName)).isNull();
    }

    @Test
    void extract_WhenTheValueIsTheWrongType_ThenIndicatesATypeMismatch() {
        var propertyName = UUID.randomUUID().toString();
        var map = Map.of(propertyName, source);
        when(conversionService.canConvert(sourceType, targetType)).thenReturn(false);
        assertThat(fixture.extract(map, method, propertyName)).isEqualTo(PropertyExtractor.TYPE_MISMATCH);
    }

}
