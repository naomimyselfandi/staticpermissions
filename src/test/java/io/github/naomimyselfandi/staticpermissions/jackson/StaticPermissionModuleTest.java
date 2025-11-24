package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaticPermissionModuleTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Module.SetupContext setupContext;

    @Mock
    private StaticPermissionService staticPermissionService;

    @Mock
    private ObjectProvider<ObjectMapper> objectMappers;

    @InjectMocks
    private StaticPermissionModule fixture;

    @Test
    void getModuleName() {
        assertThat(fixture.getModuleName()).isEqualTo("StaticPermissionModule");
    }

    @Test
    void version() {
        assertThat(fixture.version())
                .returns(1, Version::getMajorVersion)
                .returns(0, Version::getMinorVersion)
                .returns(0, Version::getPatchLevel)
                .returns(false, Version::isSnapshot)
                .returns("io.github.naomimyselfandi", Version::getGroupId)
                .returns("staticpermissions", Version::getArtifactId);
    }

    @Test
    void setupModule() {
        fixture.setupModule(setupContext);
        verify(setupContext).addDeserializers(new IntentDeserializers(staticPermissionService));
        verify(setupContext).addKeyDeserializers(new IntentKeyDeserializers(staticPermissionService));
    }

    @Test
    void initialize() {
        when(objectMappers.iterator()).then(invocation -> List.of(objectMapper).iterator());
        fixture.initialize();
        verify(objectMapper).registerModule(fixture);
    }

}
