package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentFactoryImplTest {

    private interface TestIntent extends Intent {}

    @Mock
    private TestIntent testIntent;

    @Mock
    private Authentication authentication;

    @Mock
    private StaticPermissionService staticPermissionService;

    private IntentFactoryImpl<TestIntent, Object> fixture;

    @BeforeEach
    void setup() {
        fixture = new IntentFactoryImpl<>(TestIntent.class, staticPermissionService);
    }

    @Test
    void require() {
        var source = new Object();
        when(staticPermissionService.require(source, TestIntent.class, authentication))
                .thenReturn(testIntent);
        assertThat(fixture.require(source, authentication)).isEqualTo(testIntent);
    }

    @Test
    void request() {
        var source = new Object();
        when(staticPermissionService.request(source, TestIntent.class, authentication))
                .thenReturn(Optional.of(testIntent));
        assertThat(fixture.request(source, authentication)).contains(testIntent);
    }

    @Test
    void require_ForCurrentUser() {
        var source = new Object();
        when(staticPermissionService.require(source, TestIntent.class)).thenReturn(testIntent);
        assertThat(fixture.require(source)).isEqualTo(testIntent);
    }

    @Test
    void request_ForCurrentUser() {
        var source = new Object();
        when(staticPermissionService.request(source, TestIntent.class)).thenReturn(Optional.of(testIntent));
        assertThat(fixture.request(source)).contains(testIntent);
    }

}
