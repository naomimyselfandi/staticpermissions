package io.github.naomimyselfandi.staticpermissions;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;

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
            classLoader.loadClass("org.springframework.web.servlet.config.annotation.WebMvcConfigurer");
            result.add("io.github.naomimyselfandi.staticpermissions.web.StaticPermissionWebConfiguration");
        } catch (ClassNotFoundException ignored) {}
        return result.toArray(String[]::new);
    }

}
