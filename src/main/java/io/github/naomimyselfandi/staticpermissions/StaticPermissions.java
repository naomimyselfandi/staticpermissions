package io.github.naomimyselfandi.staticpermissions;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class StaticPermissions implements ImportSelector {

    private final ClassLoader classLoader;

    StaticPermissions() {
        this(StaticPermissions.class.getClassLoader());
    }

    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        var result = new ArrayList<String>();
        result.add("io.github.naomimyselfandi.staticpermissions.core.StaticPermissionConfiguration");
        try {
            classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            result.add("io.github.naomimyselfandi.staticpermissions.jackson.StaticPermissionJacksonConfiguration");
        } catch (Throwable ignored) {}
        return result.toArray(String[]::new);
    }

}
