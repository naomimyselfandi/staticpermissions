package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import io.github.naomimyselfandi.staticpermissions.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.List;

import static io.github.naomimyselfandi.staticpermissions.PropertyExtractor.ValidationResult.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractorFactoryImplTest {

    private interface Foo {}

    private interface Bar {}

    private interface Source {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Source.class);
    }

    @SuppressWarnings("unused")
    private interface Target extends Intent {

        Foo foo();

        Bar bar();

        Object ignored();

        @Override boolean equals(@Nullable Object other);

        @Override int hashCode();

        @Override String toString();

    }

    private Method fooMethod, barMethod;

    private MethodRole bar = MethodRole.REQUIRED_PROPERTY;

    @Mock
    private PropertyExtractor<Source> propertyExtractor;

    @Mock
    private NamingConvention namingConvention;

    @Mock
    private ConversionService conversionService;

    @Mock
    private MethodRoleHelper methodRoleHelper;

    @Mock
    private PropertyExtractorRegistry propertyExtractorRegistry;

    @InjectMocks
    private ExtractorFactoryImpl fixture;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        fooMethod = Target.class.getMethod("foo");
        barMethod = Target.class.getMethod("bar");
        lenient().when(namingConvention.normalize(any())).then(invocation -> {
            var string = invocation.<String>getArgument(0);
            return string.toUpperCase();
        });
        lenient().when(methodRoleHelper.getRole(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            return switch (method.getName()) {
                case "equals" -> MethodRole.EQUALS;
                case "hashCode" -> MethodRole.HASH_CODE;
                case "toString" -> MethodRole.TO_STRING;
                case "__auth__" -> MethodRole.AUTHENTICATION;
                case "__data__" -> MethodRole.DATA_MAP;
                case "foo" -> MethodRole.REQUIRED_PROPERTY;
                case "bar" -> bar;
                default -> MethodRole.NON_PROPERTY;
            };
        });
        lenient().when(propertyExtractor.validate(any(), any())).thenReturn(OK);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void apply(boolean barIsOptional) {
        if (barIsOptional) {
            bar = MethodRole.OPTIONAL_PROPERTY;
        }
        when((Object) propertyExtractorRegistry.get(Source.class)).thenReturn(propertyExtractor);
        var properties = List.of(
                new PolyExtractor.Property("BAR", barMethod, barIsOptional),
                new PolyExtractor.Property("FOO", fooMethod, false)
        );
        var expected = new PolyExtractor<>(properties, propertyExtractor);
        assertThat(fixture.apply(Source.class, Target.class)).contains(expected);
    }

    @Test
    void apply_WhenThereIsOnlyOneRequiredPropertyAndTheSourceIsConvertible_ThenCreatesAMonoExtractor() {
        bar = MethodRole.OPTIONAL_PROPERTY;
        var type = TypeDescriptor.valueOf(Foo.class);
        when(conversionService.canConvert(Source.TYPE, type)).thenReturn(true);
        var expected = new MonoExtractor<Source>("foo", type, conversionService);
        assertThat(fixture.apply(Source.class, Target.class)).contains(expected);
    }

    @Test
    void apply_WhenARequiredPropertyIsAbsent_ThenDoesNotCreateAnExtractor() {
        when((Object) propertyExtractorRegistry.get(Source.class)).thenReturn(propertyExtractor);
        when(propertyExtractor.validate(any(), any())).then(invocation -> {
            var name = invocation.<String>getArgument(0);
            return name.equals("FOO") ? OK : UNAVAILABLE;
        });
        assertThat(fixture.apply(Source.class, Target.class)).isEmpty();
    }

    @Test
    void apply_WhenAnOptionalPropertyIsAbsent_ThenToleratesIt() {
        bar = MethodRole.OPTIONAL_PROPERTY;
        when((Object) propertyExtractorRegistry.get(Source.class)).thenReturn(propertyExtractor);
        when(propertyExtractor.validate(any(), any())).then(invocation -> {
            var name = invocation.<String>getArgument(0);
            return name.equals("FOO") ? OK : UNAVAILABLE;
        });
        var properties = List.of(
                new PolyExtractor.Property("BAR", barMethod, true),
                new PolyExtractor.Property("FOO", fooMethod, false)
        );
        var expected = new PolyExtractor<>(properties, propertyExtractor);
        assertThat(fixture.apply(Source.class, Target.class)).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void apply_WhenAValueIsTheWrongType_ThenDoesNotCreateAnExtractor(boolean barIsOptional) {
        if (barIsOptional) {
            bar = MethodRole.OPTIONAL_PROPERTY;
        }
        when((Object) propertyExtractorRegistry.get(Source.class)).thenReturn(propertyExtractor);
        when(propertyExtractor.validate(any(), any())).then(invocation -> {
            var name = invocation.<String>getArgument(0);
            return name.equals("FOO") ? OK : TYPE_MISMATCH;
        });
        assertThat(fixture.apply(Source.class, Target.class)).isEmpty();
    }

}
