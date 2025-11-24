package io.github.naomimyselfandi.staticpermissions;

import org.springframework.security.core.Authentication;

import java.lang.annotation.*;
import java.util.Map;

/**
 * A description of an operation requiring permission checks. Instances of an
 * intent type should not be manually constructed; they should be obtained from
 * a source that checks permissions, of which this library provides several. If
 * an application obtains intent instances exclusively from such sources, and
 * uses intent types as arguments to its sensitive services, it has a static
 * guarantee that any call to those services has had permission checks applied.
 *
 * <p>See {@code README.md} for full information.</p>
 *
 * @apiNote Method names beginning and ending with two underscores are reserved
 * for internal use. These methods are part of this interface's API, and client
 * code may call them; however, an extension of this interface must not declare
 * methods with such a name.
 */
public interface Intent {

    /**
     * Indicate that the annotated method is not a property. This may be used on
     * `default` methods without parameters whose `default` implementations have
     * a utility function that should not be inadvertently changed. (If a method
     * has parameters, it is never a property, with or without this annotation.)
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotProperty {}

    /**
     * Get the authentication for which an intent object was created.
     *
     * @implSpec This is an alias for {@link #__auth__()}.
     *
     * @param intent Any intent object.
     * @return The authentication for which the intent object was created.
     */
    static Authentication getAuthentication(Intent intent) {
        return intent.__auth__();
    }

    /**
     * Get the authentication for which this intent object was created.
     *
     * @apiNote This method is named to avoid conflicts with intent properties.
     * Consider using {@link #getAuthentication(Intent)} instead of calling this
     * method directly.
     *
     * @return The user for whom this intent object was created.
     */
    @NotProperty
    Authentication __auth__();

    /**
     * View this intent object as a map of its properties.
     *
     * @apiNote This method is named to avoid conflicts with intent properties.
     * Client code may use it if needed, but should use regular getter methods
     * when possible.
     *
     * @return A map of this intent object's properties.
     */
    @NotProperty
    Map<String, Object> __data__();

}
