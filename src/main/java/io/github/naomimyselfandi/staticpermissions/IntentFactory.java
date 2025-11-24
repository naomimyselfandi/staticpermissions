package io.github.naomimyselfandi.staticpermissions;

import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * A simple source of intent instances. If {@code I} is an intent interface and
 * {@code T} is a type which converts to {@code I} according to the rules
 * described in {@code README.md}, an {@code IntentFactory<T, I>} may be
 * autowired. This is a type-safe alternative to creating intent instances from
 * the {@link StaticPermissionService} or Spring's conversion service.
 *
 * @param <T> The type of source object to create intent instances from.
 * @param <I> The type of intent interface to create.
 */
public interface IntentFactory<T, I extends Intent> {

    /**
     * Convert a source object to an intent object. All permission checks are
     * performed for the authenticated user.
     *
     * @param source The object to convert to an intent object.
     * @return The converted intent object.
     * @throws RuntimeException if a permission check fails. The specific kind
     * of exception is specified by the policy which denied the request.
     */
    I require(T source);

    /**
     * Attempt to convert a source object to an intent object. All permission
     * checks are performed for the authenticated user.
     *
     * @param source The object to convert to an intent object.
     * @return The converted intent object if all permission checks succeed, or
     * an empty optional if a permission check fails.
     */
    Optional<I> request(T source);

    /**
     * Convert a source object to an intent object.
     *
     * @param source The object to convert to an intent object.
     * @param authentication The user to perform permission checks for.
     * @return The converted intent object.
     * @throws RuntimeException if a permission check fails. The specific kind
     * of exception is specified by the policy which denied the request.
     */
    I require(T source, Authentication authentication);

    /**
     * Attempt to convert a source object to an intent object.
     *
     * @param source The object to convert to an intent object.
     * @param authentication The user to perform permission checks for.
     * @return The converted intent object if all permission checks succeed, or
     * an empty optional if a permission check fails.
     */
    Optional<I> request(T source, Authentication authentication);

}
