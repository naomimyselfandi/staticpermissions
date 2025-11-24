package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentDeserializerTest {

    private interface TestIntent extends Intent {}

    @Mock
    private TestIntent intent;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private StaticPermissionService staticPermissionService;

    private IntentDeserializer<TestIntent> fixture;

    @BeforeEach
    void setup() {
        fixture = new IntentDeserializer<>(TestIntent.class, staticPermissionService);
    }

    @Test
    void deserialize() throws IOException {
        var node = new ObjectNode(JsonNodeFactory.instance, Map.of(
                UUID.randomUUID().toString(), new TextNode(UUID.randomUUID().toString()),
                UUID.randomUUID().toString(), new TextNode(UUID.randomUUID().toString())
        ));
        when(staticPermissionService.require(node, TestIntent.class)).thenReturn(intent);
        when(jsonParser.readValueAsTree()).thenReturn(node);
        assertThat(fixture.deserialize(jsonParser, null)).isEqualTo(intent);
    }

}
