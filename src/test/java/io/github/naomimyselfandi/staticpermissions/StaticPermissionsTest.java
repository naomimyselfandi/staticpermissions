package io.github.naomimyselfandi.staticpermissions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticPermissionsTest {

    private static final String JACKSON = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String WEB_MVC = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer";

    @Mock
    private ClassLoader loader;

    @Mock
    private AnnotationMetadata importingClassMetadata;

    @Test
    void selectImports() {
        assertThat(new StaticPermissions().selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration",
                "io.github.naomimyselfandi.staticpermissions.jackson.StaticPermissionJacksonConfiguration",
                "io.github.naomimyselfandi.staticpermissions.web.StaticPermissionWebConfiguration"
        );
    }

    @Test
    void selectImports_WhenJacksonIsUnavailable_ThenSkipsTheIntegration() throws ClassNotFoundException {
        when(loader.loadClass(JACKSON)).thenThrow(ClassNotFoundException.class);
        assertThat(new StaticPermissions(loader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration"
        );
        verify(loader, never()).loadClass(WEB_MVC);
    }

    @Test
    void selectImports_WhenWebMvcIsUnavailable_ThenSkipsTheIntegration() throws ClassNotFoundException {
        when((Object) loader.loadClass(JACKSON)).thenReturn(ObjectMapper.class);
        when(loader.loadClass(WEB_MVC)).thenThrow(ClassNotFoundException.class);
        assertThat(new StaticPermissions(loader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration",
                "io.github.naomimyselfandi.staticpermissions.jackson.StaticPermissionJacksonConfiguration"
        );
    }

}
