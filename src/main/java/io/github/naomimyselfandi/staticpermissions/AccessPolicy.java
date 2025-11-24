package io.github.naomimyselfandi.staticpermissions;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.util.function.Supplier;

/**
 * A policy that controls some type of permission. When an intent object is
 * created, all appropriate policies have a chance to deny the request.
 *
 * <p>Access policies are covariant: if {@code B} extends {@code A}, an access
 * policy for {@code A} also applies to {@code B}. More general access policies
 * are checked first, and when a policy denies access, any remaining policies
 * are skipped. Policies may be registered simply by defining them as Spring
 * beans.</p>
 *
 * @param <I> The type of intent to which this policy applies.
 */
public interface AccessPolicy<I> {

    @FunctionalInterface
    interface Denial extends Supplier<RuntimeException> {}

    /**
     * Check if an intent is permitted for some user. Implementations may return
     * {@code null} to permit access, or a non-null value to deny access.
     *
     * @param intent The intent being checked.
     * @return A callback that creates an exception describing the reason for
     * denial, if access is denied, or {@code null} if access is permitted.
     */
    @Nullable Denial apply(I intent);

    /**
     * Get the intent type to which this policy applies.
     *
     * @implNote The default implementation resolves the type reflectively. This
     * will not work if type information is unavailable, typically because this
     * implementation is generic or a lambda.
     *
     * @return The intent type to which this policy applies.
     */
    default Class<I> getIntentType() {
        @SuppressWarnings("unchecked")
        var type = (Class<I>) ResolvableType.forClass(AccessPolicy.class, getClass()).getGeneric().toClass();
        return type;
    }

}
