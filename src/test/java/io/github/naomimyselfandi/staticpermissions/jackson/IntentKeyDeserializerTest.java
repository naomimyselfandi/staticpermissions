package io.github.naomimyselfandi.staticpermissions.jackson;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentKeyDeserializerTest {

    private interface TestIntent extends Intent {}

    @Mock
    private TestIntent intent;

    @Mock
    private StaticPermissionService staticPermissionService;

    private IntentKeyDeserializer<TestIntent> fixture;

    @BeforeEach
    void setup() {
        fixture = new IntentKeyDeserializer<>(TestIntent.class, staticPermissionService);
    }

    @Test
    void deserializeKey() {
        var string = UUID.randomUUID().toString();
        when(staticPermissionService.require(string, TestIntent.class)).thenReturn(intent);
        assertThat(fixture.deserializeKey(string, null)).isEqualTo(intent);
    }

}
