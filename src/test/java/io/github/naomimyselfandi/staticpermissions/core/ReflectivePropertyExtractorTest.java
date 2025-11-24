package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectivePropertyExtractorTest {

    @Mock
    private ConversionService conversionService;

    @Mock
    private NamingConvention namingConvention;

    private ReflectivePropertyExtractor<Source> fixture;

    private enum Pikachu {
        PIKACHU;
        static final TypeDescriptor TYPE = TypeDescriptor.valueOf(Pikachu.class);
    }

    private enum Raichu {
        RAICHU;
        static final TypeDescriptor TYPE = TypeDescriptor.valueOf(Raichu.class);
    }

    @SuppressWarnings("unused")
    private interface Source {

        Pikachu basic();

        Raichu stage1();

        // None of these methods are eligible.

        Pikachu stage2(Object unexpectedArgument);

        static Pikachu stage2() {
            return fail();
        }

    }

    @SuppressWarnings("unused")
    private interface Target {

        Pikachu pikachu();

        Raichu raichu();

    }

    @BeforeEach
    void setup() {
        lenient().when(conversionService.canConvert(any(TypeDescriptor.class), any())).then(invocation -> {
            var sourceType = invocation.getArgument(0);
            var targetType = invocation.getArgument(1);
            return sourceType.equals(targetType) || (sourceType.equals(Pikachu.TYPE) && targetType.equals(Raichu.TYPE));
        });
        lenient().when(conversionService.convert(any(), any(), any())).then(invocation -> {
            var source = invocation.getArgument(0);
            var sourceType = invocation.<TypeDescriptor>getArgument(1);
            var targetType = invocation.<TypeDescriptor>getArgument(2);
            assertThat(source).isInstanceOf(sourceType.getObjectType());
            assertThat(conversionService.canConvert(sourceType, targetType)).isTrue();
            return targetType.getObjectType().getEnumConstants()[0];
        });
        when(namingConvention.normalize(any())).then(i -> i.<String>getArgument(0).toUpperCase());
        fixture = new ReflectivePropertyExtractor<>(conversionService, namingConvention, Source.class);
    }

    @MethodSource
    @ParameterizedTest
    void validate(String name, TypeDescriptor type, ReflectivePropertyExtractor.ValidationResult expected) {
        assertThat(fixture.validate(name, type)).isEqualTo(expected);
    }

    @MethodSource
    @ParameterizedTest
    void extract(String name, Method method, Object expected) {
        var helper = mock(Source.class);
        lenient().when(helper.basic()).thenReturn(Pikachu.PIKACHU);
        lenient().when(helper.stage1()).thenReturn(Raichu.RAICHU);
        assertThat(fixture.extract(helper, method, name)).isEqualTo(expected);
    }

    private static Stream<Arguments> validate() {
        return Stream.of(
                arguments("BASIC", Pikachu.TYPE, ReflectivePropertyExtractor.ValidationResult.OK),
                arguments("STAGE1", Raichu.TYPE, ReflectivePropertyExtractor.ValidationResult.OK),
                arguments("BASIC", Raichu.TYPE, ReflectivePropertyExtractor.ValidationResult.OK),
                arguments("STAGE1", Pikachu.TYPE, ReflectivePropertyExtractor.ValidationResult.TYPE_MISMATCH),
                arguments("STAGE2", Raichu.TYPE, ReflectivePropertyExtractor.ValidationResult.UNAVAILABLE)
        );
    }

    private static Stream<Arguments> extract() throws NoSuchMethodException {
        var pikachu = Target.class.getMethod("pikachu");
        var raichu = Target.class.getMethod("raichu");
        return Stream.of(
                arguments("BASIC", pikachu, Pikachu.PIKACHU),
                arguments("STAGE1", raichu, Raichu.RAICHU),
                arguments("BASIC", raichu, Raichu.RAICHU),
                arguments("STAGE1", pikachu, PropertyExtractor.TYPE_MISMATCH),
                arguments("STAGE2", raichu, null)
        );
    }

}
