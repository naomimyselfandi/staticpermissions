package io.github.naomimyselfandi.staticpermissions.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Merge an intent object from multiple parts of a request.
 *
 * <p>Internally, this merging uses a Jackson {@code ObjectNode}. The merging
 * begins by either reading the request body as an {@code ObjectNode} or
 * constructing an empty {@code ObjectNode}. Any number of path variables,
 * request parameters, headers, or cookie values are then injected into the
 * {@code ObjectNode}. The key to inject them as may be explicitly specified or
 * inferred from the path variable, request parameter, header, or cookie's name.
 * Once all values are injected, the {@code ObjectNode} is converted to an
 * intent object of the parameter's type</p>
 *
 * <p>Since these data sources are untyped, an appropriate {@code JsonNode} type
 * is inferred for each value. For example, {@code 42} is represented as an
 * {@code IntNode}, {@code true} is represented as a {@code BooleanNode}, and
 * {@code foo} is represented as a {@code TextNode}. Request parameters and
 * headers are typically trimmed to the first value (if any), but may be mapped
 * as {@code ArrayNode}s to retain all values. Consult the individual annotation
 * parameters for further information.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MergedIntent {

    /**
     * Specify the request body mapping. Three mappings are supported:
     * <ul>
     * <li>If this is the empty string, the request body is not consumed, and
     * merging begins with an empty {@code ObjectNode}.</li>
     * <li>If this is exactly a single asterisk, the request body is read as an
     * {@code ObjectNode}, and merging begins with this node.</li>
     * <li>If this is any other value, merging begins with an empty
     * {@code ObjectNode}. The request body is read as a {@code JsonNode} (of
     * any type) and injected into the {@code ObjectNode} using the given value
     * as the key. For example, {@code body = "content"} injects the request
     * body at the key {@code content}.</li>
     * </ul>
     * @return A request body mapping specification.
     */
    String body() default "";

    /**
     * Specify the path variable mappings. Each entry in this array specifies
     * the name of a path variable and the key at which it is injected. These
     * are usually given as {@code key:pathVariable}; if the mapping does not
     * contain a colon, it indicates that the key and path variable name are the
     * same.
     *
     * <p>For example, <code>path = {"foo", "baz:bar"}</code> specifies that the
     * path variables {@code foo} and {@code bar} are used, and that they are
     * injected as {@code foo} and {@code baz} respectively.</p>
     *
     * <p>If this is empty, all path variables are used.</p>
     *
     * @return The path variable mapping specification.
     */
    String[] path() default {};

    /**
     * Specify the request parameter mappings. Each entry in this array
     * specifies the name of a request parameter and the key at which it is
     * injected. These are usually given as {@code key:parameter}; if the
     * mapping does not contain a colon, it indicates that the key and parameter
     * name are the same.
     *
     * <p>If a request contains multiple values for a parameter, by default,
     * only the first value is used. This may be changed by prefixing the
     * mapping with an asterisk, which causes all values to be injected as an
     * {@code ArrayNode}.</p>
     *
     * <p>For example, <code>params = {"*foo", "baz:bar"}</code> specifies that
     * the cookie {@code foo} and {@code bar} are used, that they are injected
     * as {@code foo} and {@code baz} respectively, and that all values for
     * {@code foo} are used, while only the first value for {@code bar} is used.
     * </p>
     *
     * <p>If this is empty, all request parameters are ignored.</p>
     *
     * @return The request parameter mapping specification.
     */
    String[] params() default {};

    /**
     * Specify the header mappings. Each entry in this array specifies the name
     * of a header and the key at which it is injected. These are usually given
     * as {@code key:header}. Like the other annotation parameters, if the
     * mapping does not contain a colon, it indicates that the key and header
     * name are the same, but this is rarely used with headers.
     *
     * <p>If a request contains multiple values for a header, by default, only
     * the first value is used. This may be changed by prefixing the mapping
     * with an asterisk, which causes all values to be injected as an
     * {@code ArrayNode}.</p>
     *
     * <p>For example, <code>headers = {"*foo:X-Foo", "bar:X-Bar"}</code>
     * specifies that the headers{@code X-Foo} and {@code X-Bar} are used, that
     * they are injected as {@code foo} and {@code bar} respectively, and that
     * all values for {@code foo} are used, while only the first value for
     * {@code bar} is used.</p>
     *
     * <p>If this is empty, all headers are ignored.</p>
     *
     * @return The request parameter mapping specification.
     */
    String[] headers() default {};

    /**
     * Specify the cookie mappings. Each entry in this array specifies the name
     * of a cookie and the key at which it is injected. These are usually given
     * as {@code key:cookie}; if the mapping does not contain a colon, it
     * indicates that the key and cookie name are the same.
     *
     * <p>For example, <code>cookies = {"foo", "baz:bar"}</code> specifies that
     * the cookies {@code foo} and {@code bar} are used, and that they are
     * injected as {@code foo} and {@code baz} respectively.</p>
     *
     * <p>If this is empty, all cookies are ignored.</p>
     *
     * @return The cookie mapping specification.
     */
    String[] cookies() default {};

}
