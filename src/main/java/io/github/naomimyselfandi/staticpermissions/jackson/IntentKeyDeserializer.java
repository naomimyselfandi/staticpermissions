package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.*;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
final class IntentKeyDeserializer<I extends Intent> extends KeyDeserializer {

    private final Class<I> intentType;
    private final StaticPermissionService staticPermissionService;

    @Override
    public Object deserializeKey(String key, @Nullable DeserializationContext context) {
        return staticPermissionService.require(key, intentType);
    }

}

