package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
final class IntentKeyDeserializers implements KeyDeserializers {

    private final StaticPermissionService staticPermissionService;

    @Override
    public @Nullable KeyDeserializer findKeyDeserializer(
            JavaType javaType,
            @Nullable DeserializationConfig config,
            @Nullable BeanDescription beanDesc
    ) {
        var type = javaType.getRawClass();
        if (staticPermissionService.isSourceFor(String.class, type)) {
            return new IntentKeyDeserializer<>(type.asSubclass(Intent.class), staticPermissionService);
        } else {
            return null;
        }
    }

}
