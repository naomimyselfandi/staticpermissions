package io.github.naomimyselfandi.staticpermissions;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * The low-level service that manages permission checks. This service is
 * provided for advanced use cases, but interacting with it directly is rare.
 * Instead, consider autowiring an {@link IntentFactory}, using Spring's
 * conversion service, or receiving intent objects as controller method
 * arguments in a web context.
 */
public interface StaticPermissionService {

    /**
     * Check if some type can be converted to some intent type.
     *
     * @param source The type to convert from.
     * @param type The intent type to convert to.
     * @return True if the conversion is valid, and false otherwise. This method
     * always returns false if the second argument is not an intent type.
     */
    boolean isSourceFor(Class<?> source, Class<?> type);

    /**
     * Convert a source object to an intent object. All permission checks are
     * performed for the authenticated user.
     *
     * @param source The object to convert to an intent object.
     * @param type The intent type to convert to.
     * @return The converted intent object.
     * @throws RuntimeException if a permission check fails. The specific kind
     * of exception is specified by the policy which denied the request.
     */
    <I extends Intent> I require(Object source, Class<I> type);

    /**
     * Attempt to convert a source object to an intent object. All permission
     * checks are performed for the authenticated user.
     *
     * @param source The object to convert to an intent object.
     * @param type The intent type to convert to.
     * @return The converted intent object if all permission checks succeed, or
     * an empty optional if a permission check fails.
     */
    <I extends Intent> Optional<I> request(Object source, Class<I> type);

    /**
     * Convert a source object to an intent object.
     *
     * @param source The object to convert to an intent object.
     * @param authentication The user to perform permission checks for.
     * @param type The intent type to convert to.
     * @return The converted intent object.
     * @throws RuntimeException if a permission check fails. The specific kind
     * of exception is specified by the policy which denied the request.
     */
    default <I extends Intent> I require(Object source, Class<I> type, Authentication authentication) {
        var originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return require(source, type);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }

    /**
     * Attempt to convert a source object to an intent object.
     *
     * @param source The object to convert to an intent object.
     * @param type The intent type to convert to.
     * @param authentication The user to perform permission checks for.
     * @return The converted intent object if all permission checks succeed, or
     * an empty optional if a permission check fails.
     */
    default <I extends Intent> Optional<I> request(Object source, Class<I> type, Authentication authentication) {
        var originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request(source, type);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }

    /**
     * Remove any {@code get} or {@code is} prefix from a method's name.
     * @param method The method whose name should be normalized.
     * @return The method name without any prefix.
     */
    String normalizeMethodName(Method method);

}
