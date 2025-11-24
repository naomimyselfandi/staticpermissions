package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

import java.io.IOException;

@EqualsAndHashCode(callSuper = false)
final class IntentDeserializer<I extends Intent> extends StdDeserializer<I> {

    private final Class<I> intentType;
    private final StaticPermissionService staticPermissionService;

    IntentDeserializer(Class<I> intentType, StaticPermissionService staticPermissionService) {
        super(intentType);
        this.intentType = intentType;
        this.staticPermissionService = staticPermissionService;
    }

    @Override
    public I deserialize(JsonParser parser, @Nullable DeserializationContext context) throws IOException {
        return staticPermissionService.require(parser.readValueAsTree(), intentType);
    }

}
