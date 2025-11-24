package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.TypeDescriptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentConverterTest {

    private interface Source {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Source.class);
    }

    private interface Target extends Intent {
        TypeDescriptor TYPE = TypeDescriptor.valueOf(Target.class);
    }

    @Mock
    private Target intent;

    @Mock
    private StaticPermissionService staticPermissionService;

    @InjectMocks
    private IntentConverter fixture;

    @Test
    void getConvertibleTypes() {
        assertThat(fixture.getConvertibleTypes()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void matches(boolean valid) {
        when(staticPermissionService.isSourceFor(Source.class, Target.class)).thenReturn(valid);
        assertThat(fixture.matches(Source.TYPE, Target.TYPE)).isEqualTo(valid);
    }

    @Test
    void convert() {
        var source = new Source() {};
        when(staticPermissionService.require(source, Target.class)).thenReturn(intent);
        assertThat(fixture.convert(source, Source.TYPE, Target.TYPE)).isEqualTo(intent);
    }

    @Test
    void convert_WhenTheSourceIsNull_ThenNull() {
        assertThat(fixture.convert(null, Source.TYPE, Target.TYPE)).isNull();
    }

}
