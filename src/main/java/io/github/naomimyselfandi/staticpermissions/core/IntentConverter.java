package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;

import java.util.Set;

@EqualsAndHashCode
@RequiredArgsConstructor
class IntentConverter implements ConditionalGenericConverter {

    private final StaticPermissionService staticPermissionService;

    @Override
    public @Nullable Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return staticPermissionService.isSourceFor(sourceType.getObjectType(), targetType.getObjectType());
    }

    @Override
    public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else {
            return staticPermissionService.require(source, targetType.getObjectType().asSubclass(Intent.class));
        }
    }

}
