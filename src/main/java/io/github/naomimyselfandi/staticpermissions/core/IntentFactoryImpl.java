package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.IntentFactory;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@RequiredArgsConstructor
class IntentFactoryImpl<I extends Intent, T> implements IntentFactory<T, I> {

    final Class<I> type;
    final StaticPermissionService staticPermissionService;

    @Override
    public I require(T source) {
        return staticPermissionService.require(source, type);
    }

    @Override
    public Optional<I> request(T source) {
        return staticPermissionService.request(source, type);
    }

    @Override
    public I require(T source, Authentication authentication) {
        return staticPermissionService.require(source, type, authentication);
    }

    @Override
    public Optional<I> request(T source, Authentication authentication) {
        return staticPermissionService.request(source, type, authentication);
    }

}
