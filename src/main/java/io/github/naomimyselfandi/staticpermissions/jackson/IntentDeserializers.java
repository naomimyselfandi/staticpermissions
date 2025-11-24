package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
final class IntentDeserializers extends Deserializers.Base {

    private final StaticPermissionService staticPermissionService;

    @Override
    public @Nullable JsonDeserializer<?> findBeanDeserializer(
            JavaType javaType,
            @Nullable DeserializationConfig config,
            @Nullable BeanDescription beanDesc
    ) {
        var type = javaType.getRawClass();
        if (staticPermissionService.isSourceFor(JsonNode.class, type)) {
            return new IntentDeserializer<>(type.asSubclass(Intent.class), staticPermissionService);
        } else {
            return null;
        }
    }

}
