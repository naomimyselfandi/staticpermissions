package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentDeserializersTest {

    private interface TestIntent extends Intent {}

    @Mock
    private StaticPermissionService staticPermissionService;

    @InjectMocks
    private IntentDeserializers fixture;

    @Test
    void findBeanDeserializer() {
        when(staticPermissionService.isSourceFor(JsonNode.class, TestIntent.class)).thenReturn(true);
        var javaType = TypeFactory.defaultInstance().constructType(TestIntent.class);
        var expected = new IntentDeserializer<>(TestIntent.class, staticPermissionService);
        assertThat(fixture.findBeanDeserializer(javaType, null, null)).isEqualTo(expected);
    }

    @Test
    void findBeanDeserializer_WhenTheTypeIsNotAppropriate_ThenNull() {
        when(staticPermissionService.isSourceFor(JsonNode.class, TestIntent.class)).thenReturn(false);
        var javaType = TypeFactory.defaultInstance().constructType(TestIntent.class);
        assertThat(fixture.findBeanDeserializer(javaType, null, null)).isNull();
    }

}
