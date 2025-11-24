package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import static io.github.naomimyselfandi.staticpermissions.PropertyExtractor.TYPE_MISMATCH;

@ExtendWith(MockitoExtension.class)
class PolyExtractorTest {

    private interface Source {}

    private interface RequiredPropertyType {
        @SuppressWarnings("unused")
        RequiredPropertyType requiredMethodName();
        Method METHOD = Arrays.stream(RequiredPropertyType.class.getMethods()).findFirst().orElseThrow();
    }

    private interface OptionalPropertyType {
        @SuppressWarnings("unused")
        OptionalPropertyType optionalMethodName();
        Method METHOD = Arrays.stream(OptionalPropertyType.class.getMethods()).findFirst().orElseThrow();
    }

    private String requiredName, optionalName;

    @Mock
    private Source source;

    @Mock
    private RequiredPropertyType requiredPropertyValue;

    @Mock
    private OptionalPropertyType optionalPropertyValue;

    @Mock
    private PropertyExtractor<Source> propertyExtractor;

    private PolyExtractor<Source> fixture;

    @BeforeEach
    void setup() {
        requiredName = UUID.randomUUID().toString();
        optionalName = UUID.randomUUID().toString();
        var requiredProperty = new PolyExtractor.Property(requiredName, RequiredPropertyType.METHOD, false);
        var optionalProperty = new PolyExtractor.Property(optionalName, OptionalPropertyType.METHOD, true);
        fixture = new PolyExtractor<>(List.of(requiredProperty, optionalProperty), propertyExtractor);
    }

    @Test
    void extract() {
        when(propertyExtractor.extract(source, RequiredPropertyType.METHOD, requiredName)).thenReturn(requiredPropertyValue);
        when(propertyExtractor.extract(source, OptionalPropertyType.METHOD, optionalName)).thenReturn(optionalPropertyValue);
        assertThat(fixture.extract(source))
                .hasSize(2)
                .containsEntry(RequiredPropertyType.METHOD.getName(), requiredPropertyValue)
                .containsEntry(OptionalPropertyType.METHOD.getName(), optionalPropertyValue)
                .isUnmodifiable();
    }

    @Test
    void extract_WhenARequiredPropertyIsUnavailable_ThenThrows() {
        when(propertyExtractor.extract(source, RequiredPropertyType.METHOD, requiredName)).thenReturn(null);
        assertThatThrownBy(() -> fixture.extract(source))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Couldn't find a value for '%s' in %s.", requiredName, source);
    }

    @Test
    void extract_WhenAnOptionalPropertyIsUnavailable_ThenSkipsIt() {
        when(propertyExtractor.extract(source, RequiredPropertyType.METHOD, requiredName)).thenReturn(requiredPropertyValue);
        when(propertyExtractor.extract(source, OptionalPropertyType.METHOD, optionalName)).thenReturn(null);
        assertThat(fixture.extract(source))
                .hasSize(1)
                .containsEntry(RequiredPropertyType.METHOD.getName(), requiredPropertyValue)
                .isUnmodifiable();
    }

    @Test
    void extract_WhenARequiredPropertyIsIncorrectlyTyped_ThenThrows() {
        when(propertyExtractor.extract(source, RequiredPropertyType.METHOD, requiredName)).thenReturn(TYPE_MISMATCH);
        assertThatThrownBy(() -> fixture.extract(source))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Value for '%s' in %s is not the correct type.", requiredName, source);
    }

    @Test
    void extract_WhenAnOptionalPropertyIsIncorrectlyTyped_ThenThrows() {
        when(propertyExtractor.extract(source, RequiredPropertyType.METHOD, requiredName)).thenReturn(requiredPropertyValue);
        when(propertyExtractor.extract(source, OptionalPropertyType.METHOD, optionalName)).thenReturn(TYPE_MISMATCH);
        assertThatThrownBy(() -> fixture.extract(source))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Value for '%s' in %s is not the correct type.", optionalName, source);
    }

}
