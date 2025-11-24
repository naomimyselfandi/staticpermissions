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

    @Mock
    private ClassLoader classLoader;

    @Mock
    private AnnotationMetadata importingClassMetadata;

    @Test
    void selectImports() {
        assertThat(new StaticPermissions().selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration",
                "io.github.naomimyselfandi.staticpermissions.jackson.StaticPermissionJacksonConfiguration"
        );
    }

    @Test
    void selectImports_WhenJacksonIsUnavailable_ThenSkipsTheIntegration() throws ClassNotFoundException {
        when(classLoader.loadClass(any())).then(invocation -> {
            assertThat(invocation.<String>getArgument(0)).isEqualTo(ObjectMapper.class.getCanonicalName());
            throw new ClassNotFoundException();
        });
        assertThat(new StaticPermissions(classLoader).selectImports(importingClassMetadata)).containsExactly(
                "io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration"
        );
    }

}
