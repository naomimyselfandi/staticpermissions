package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class StaticPermissionModule extends com.fasterxml.jackson.databind.Module {

    private final StaticPermissionService staticPermissionService;
    private final ObjectProvider<ObjectMapper> objectMappers;

    @Override
    public String getModuleName() {
        return "StaticPermissionModule";
    }

    @Override
    public Version version() {
        var groupId = "io.github.naomimyselfandi";
        var artifactId = "staticpermissions";
        return new Version(1, 0,0, null, groupId, artifactId);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new IntentDeserializers(staticPermissionService));
        context.addKeyDeserializers(new IntentKeyDeserializers(staticPermissionService));
    }

    @PostConstruct
    void initialize() {
        for (var objectMapper : objectMappers) {
            objectMapper.registerModule(this);
        }
    }

}
