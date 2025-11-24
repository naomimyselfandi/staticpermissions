package io.github.naomimyselfandi.staticpermissions.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonoExtractorTest {

    private interface Target {}

    @Mock
    private Target target;

    private String name;

    private TypeDescriptor targetType;

    @Mock
    private ConversionService conversionService;

    private MonoExtractor<Object> fixture;

    @BeforeEach
    void setup() {
        name = UUID.randomUUID().toString();
        targetType = TypeDescriptor.valueOf(Target.class);
        fixture = new MonoExtractor<>(name, targetType, conversionService);
    }

    @Test
    void extract() {
        var source = new Object();
        when(conversionService.convert(source, targetType)).thenReturn(target);
        assertThat(fixture.extract(source)).hasSize(1).containsEntry(name, target).isUnmodifiable();
    }

}
