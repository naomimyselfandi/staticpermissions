package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentKeyDeserializersTest {

    private interface TestIntent extends Intent {}

    @Mock
    private StaticPermissionService staticPermissionService;

    @InjectMocks
    private IntentKeyDeserializers fixture;

    @Test
    void findKeyDeserializer() {
        when(staticPermissionService.isSourceFor(String.class, TestIntent.class)).thenReturn(true);
        var javaType = TypeFactory.defaultInstance().constructType(TestIntent.class);
        var expected = new IntentKeyDeserializer<>(TestIntent.class, staticPermissionService);
        assertThat(fixture.findKeyDeserializer(javaType, null, null)).isEqualTo(expected);
    }

    @Test
    void findKeyDeserializer_WhenTheTypeIsNotAppropriate_ThenFalse() {
        when(staticPermissionService.isSourceFor(String.class, TestIntent.class)).thenReturn(false);
        var javaType = TypeFactory.defaultInstance().constructType(TestIntent.class);
        assertThat(fixture.findKeyDeserializer(javaType, null, null)).isNull();
    }

}
