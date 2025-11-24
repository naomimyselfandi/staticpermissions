package io.github.naomimyselfandi.staticpermissions;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

/**
 * A service provider interface for instantiating intent types. Ordinarily, an
 * intent type's property values are extracted reflectively. Implementations of
 * this interface provide alternative extraction strategies, such as extracting
 * elements of a {@code Map} or {@code JsonNode}.
 *
 * @param <T> The type from which this implementation can extract values.
 */
public interface PropertyExtractor<T> {

    /**
     * The result of validating a property's availability.
     */
    enum ValidationResult {

        /**
         * The value is available as the correct type.
         */
        OK,

        /**
         * The value is unavailable.
         */
        UNAVAILABLE,

        /**
         * The value is available, but as a different type.
         */
        TYPE_MISMATCH,

    }

    /**
     * A marker value indicating that an object is available but the wrong type.
     */
    Object TYPE_MISMATCH = ValidationResult.TYPE_MISMATCH;

    /**
     * Check if this extractor can satisfy a property. This is called during
     * early validation, so no specific source object is available. If this
     * implementation cannot determine whether it can satisfy a property, it
     * should return {@link ValidationResult#OK}.
     *
     * @param propertyName The property's name.
     * @param propertyType The property's type.
     * @return A result indicating whether the property can be satisfied.
     * @implSpec The default
     */
    default ValidationResult validate(String propertyName, TypeDescriptor propertyType) {
        return ValidationResult.OK;
    }

    /**
     * Extract a property value from some object.
     *
     * @param source The object to extract from.
     * @param method The intent type method defining the property. The extracted
     *               value must be an instance of the method's return type, and
     *               annotations on the method may be used as appropriate.
     * @param propertyName The property's name. This may be different from the
     *                     method's name, typically because a {@code get} or
     *                     {@code is} prefix was removed.
     * @return The extracted value, converted to the method's return type; or
     * {@code null} if no value is available; or {@link #TYPE_MISMATCH} if it
     * is available but cannot be converted to the appropriate type.
     */
    @Nullable Object extract(T source, Method method, String propertyName);

    /**
     * Get the type from which this implementation can extract values.
     *
     * @implNote The default implementation resolves the type reflectively. This
     * will not work if type information is unavailable, typically because this
     * implementation is generic or a lambda, in which case this implementation
     * must be overwritten.
     *
     * @return The type from which this implementation can extract values.
     */
    default Class<T> getSupportedType() {
        @SuppressWarnings("unchecked")
        var type = (Class<T>) ResolvableType.forClass(PropertyExtractor.class, getClass()).getGeneric().toClass();
        return type;
    }

}
