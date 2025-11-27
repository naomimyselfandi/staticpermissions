package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class JsonNodeHelperImplTest {

    private JsonNodeHelperImpl fixture;

    @BeforeEach
    void setup() {
        fixture = new JsonNodeHelperImpl();
    }

    @MethodSource
    @ParameterizedTest
    void infer(@Nullable String input, JsonNode expected) {
        assertThat(fixture.infer(input)).isEqualTo(expected);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static Stream<Arguments> infer() {
        long maxInt = Integer.MAX_VALUE;
        long minInt = Integer.MIN_VALUE;
        return Stream.of(
                arguments(null, NullNode.getInstance()),
                arguments("true", BooleanNode.TRUE),
                arguments("false", BooleanNode.FALSE),
                arguments("foo", new TextNode("foo")),
                arguments("bar", new TextNode("bar")),
                arguments("0", new IntNode(0)),
                arguments("1", new IntNode(1)),
                arguments("-1", new IntNode(-1)),
                arguments("10", new IntNode(10)),
                arguments("-10", new IntNode(-10)),
                arguments("100", new IntNode(100)),
                arguments("-100", new IntNode(-100)),
                arguments(String.valueOf(maxInt), new IntNode((int) maxInt)),
                arguments(String.valueOf(minInt), new IntNode((int) minInt)),
                arguments(String.valueOf(maxInt + 1), new LongNode(maxInt + 1)),
                arguments(String.valueOf(minInt - 1), new LongNode(minInt - 1)),
                arguments(String.valueOf(Long.MAX_VALUE), new LongNode(Long.MAX_VALUE)),
                arguments(String.valueOf(Long.MIN_VALUE), new LongNode(Long.MIN_VALUE)),
                arguments("0.0", new DoubleNode(0.0)),
                arguments("0.1", new DoubleNode(0.1)),
                arguments("-0.1", new DoubleNode(-0.1)),
                arguments("Infinity", new DoubleNode(Double.POSITIVE_INFINITY)),
                arguments("-Infinity", new DoubleNode(Double.NEGATIVE_INFINITY)),
                arguments("NaN", new DoubleNode(Double.NaN)),
                arguments("0x1.fffffffffffffP+1023", new DoubleNode(Double.MAX_VALUE)),
                arguments("0x0.0000000000001P-1022", new DoubleNode(Double.MIN_VALUE))
        );
    }

}
