package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyExtractorRegistryTest {

    private interface Foo {}
    private interface Bar {}
    private interface Baz extends Bar {}
    private interface Bat extends Bar {}

    @Mock
    private PropertyExtractor<Foo> foo;

    @Mock
    private PropertyExtractor<Bar> bar;

    @Mock
    private PropertyExtractor<Baz> baz;

    @Mock
    private ConversionService conversionService;

    @Mock
    private NamingConvention namingConvention;

    private PropertyExtractorRegistry fixture;

    @BeforeEach
    void setup() {
        when(foo.getSupportedType()).thenReturn(Foo.class);
        when(bar.getSupportedType()).thenReturn(Bar.class);
        when(baz.getSupportedType()).thenReturn(Baz.class);
        fixture = new PropertyExtractorRegistryImpl(List.of(foo, bar, baz), conversionService, namingConvention);
    }

    @Test
    void get() {
        assertThat(fixture.get(Foo.class)).isEqualTo(foo);
        assertThat(fixture.get(Bar.class)).isEqualTo(bar);
        assertThat(fixture.get(Baz.class)).isEqualTo(baz);
        assertThat(fixture.get(Bat.class)).isEqualTo(bar);
    }

    @Test
    void get_WhenNoCustomExtractorIsAvailable_ThenUsesAReflectiveExtractor() {
        interface SomethingElse {}
        assertThat(fixture.get(SomethingElse.class))
                .isInstanceOfSatisfying(ReflectivePropertyExtractor.class,
                        it -> {
                            assertThat(it.conversionService).isEqualTo(conversionService);
                            assertThat(it.namingConvention).isEqualTo(namingConvention);
                            assertThat(it.source).isEqualTo(SomethingElse.class);
                        });
    }

}
