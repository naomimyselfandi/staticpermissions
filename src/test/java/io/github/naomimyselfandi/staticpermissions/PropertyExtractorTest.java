package io.github.naomimyselfandi.staticpermissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("Convert2Lambda")
class PropertyExtractorTest {

    private interface TestIntent extends Intent {}

    private PropertyExtractor<TestIntent> fixture;

    @BeforeEach
    void setup() {
        fixture = new PropertyExtractor<>() {
            @Override
            public Object extract(@NonNull TestIntent source, @NonNull Method method, @NonNull String propertyName) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    void validate() {
        interface PropertyType {}
        var propertyName = UUID.randomUUID().toString();
        var propertyType = TypeDescriptor.valueOf(PropertyType.class);
        assertThat(fixture.validate(propertyName, propertyType)).isEqualTo(PropertyExtractor.ValidationResult.OK);
    }

    @Test
    void getSupportedType() {
        assertThat(fixture.getSupportedType()).isEqualTo(TestIntent.class);
    }

}
