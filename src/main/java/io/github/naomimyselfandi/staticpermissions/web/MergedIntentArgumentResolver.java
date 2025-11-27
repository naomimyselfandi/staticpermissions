package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@RequiredArgsConstructor
class MergedIntentArgumentResolver implements HandlerMethodArgumentResolver {

    final StaticPermissionService staticPermissionService;
    final JsonNodeHelper jsonNodeHelper;
    final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MergedIntent.class)
                && staticPermissionService.isSourceFor(JsonNode.class, parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {
        var annotation = Objects.requireNonNull(parameter.getParameterAnnotation(MergedIntent.class));
        var request = Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class));
        var data = begin(request, annotation.body());
        injectPathVariables(webRequest, data, annotation.path());
        injectFields(webRequest::getHeader, webRequest::getHeaderValues, data, annotation.headers());
        injectFields(webRequest::getParameter, webRequest::getParameterValues, data, annotation.params());
        injectCookies(request.getCookies(), data, annotation.cookies());
        return staticPermissionService.require(data, parameter.getParameterType().asSubclass(Intent.class));
    }

    private static Map.Entry<String, String> split(String mapping) {
        var parts = mapping.split(":", 2);
        return Map.entry(parts[0], parts[parts.length == 2 ? 1 : 0]);
    }

    private ObjectNode begin(HttpServletRequest request, String mapping) throws IOException {
        if (mapping.equals("*")) {
            try (var input = request.getInputStream()) {
                return objectMapper.readValue(input, ObjectNode.class);
            }
        } else if (!mapping.isEmpty()) {
            try (var input = request.getInputStream()) {
                var data = new ObjectNode(JsonNodeFactory.instance, new HashMap<>());
                data.set(mapping, objectMapper.readTree(input));
                return data;
            }
        } else {
            return new ObjectNode(JsonNodeFactory.instance, new HashMap<>());
        }
    }

    private void injectPathVariables(NativeWebRequest request, ObjectNode data, String[] mappings) {
        @SuppressWarnings("unchecked")
        var map = (Map<String, String>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST);
        if (map != null) {
            if (mappings.length == 0) {
                for (var entry : map.entrySet()) {
                    data.set(entry.getKey(), jsonNodeHelper.infer(entry.getValue()));
                }
            } else {
                for (var mapping : mappings) {
                    var entry = split(mapping);
                    data.set(entry.getKey(), jsonNodeHelper.infer(map.get(entry.getValue())));
                }
            }
        }
    }

    private void injectFields(
            Function<String, String> mono,
            Function<String, String[]> poly,
            ObjectNode data,
            String[] mappings
    ) {
        for (var mapping : mappings) {
            var usePoly = mapping.startsWith("*");
            var entry = split(usePoly ? mapping.substring(1) : mapping);
            if (usePoly) {
                var array = Stream
                        .ofNullable(poly.apply(entry.getValue()))
                        .flatMap(Arrays::stream)
                        .map(jsonNodeHelper::infer)
                        .toList();
                data.set(entry.getKey(), new ArrayNode(JsonNodeFactory.instance, array));
            } else {
                data.set(entry.getKey(), jsonNodeHelper.infer(mono.apply(entry.getValue())));
            }
        }
    }

    private void injectCookies(@Nullable Cookie[] cookies, ObjectNode data, String[] mappings) {
        if (cookies != null) {
            var map = Arrays
                    .stream(mappings)
                    .map(MergedIntentArgumentResolver::split)
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            for (var cookie : cookies) {
                var key = map.get(cookie.getName());
                if (key != null) {
                    data.set(key, jsonNodeHelper.infer(cookie.getValue()));
                }
            }
        }
    }

}
