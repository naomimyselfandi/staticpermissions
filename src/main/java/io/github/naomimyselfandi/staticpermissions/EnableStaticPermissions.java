package io.github.naomimyselfandi.staticpermissions;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable the static permission library. All available library features are
 * automatically enabled, and no further configuration is necessary.
 */
@Target(ElementType.TYPE)
@Import(StaticPermissions.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableStaticPermissions {}
