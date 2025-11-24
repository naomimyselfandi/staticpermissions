package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
final class JsonNodePropertyExtractor implements PropertyExtractor<JsonNode> {

    private final ObjectMapper objectMapper;

    @Override
    public @Nullable Object extract(JsonNode source, Method method, String propertyName) {
        var targetType = resolveJavaType(method, objectMapper.getTypeFactory());
        var node = source.get(propertyName);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        } else try {
            return objectMapper.convertValue(node, targetType);
        } catch (IllegalArgumentException thrownByConvertValue) {
            return TYPE_MISMATCH;
        }
    }

    private static JavaType resolveJavaType(Method method, TypeFactory typeFactory) {
        return resolveJavaType(ResolvableType.forMethodReturnType(method), typeFactory);
    }

    private static JavaType resolveJavaType(ResolvableType type, TypeFactory typeFactory) {
        if (type.hasGenerics()) {
            var generics = type.getGenerics();
            var javaGenerics = new JavaType[generics.length];
            for (var i = 0; i < generics.length; i++) {
                javaGenerics[i] = resolveJavaType(generics[i], typeFactory);
            }
            return typeFactory.constructParametricType(type.toClass(), javaGenerics);
        } else {
            return typeFactory.constructType(type.getType());
        }
    }

}
