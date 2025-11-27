package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StaticPermissionWebConfigurationTest {

    @Mock
    private HandlerMethodArgumentResolver resolver;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StaticPermissionService staticPermissionService;

    @InjectMocks
    private StaticPermissionWebConfiguration fixture;

    @Test
    void addArgumentResolvers() {
        var resolvers = new ArrayList<HandlerMethodArgumentResolver>();
        resolvers.add(resolver);
        fixture.addArgumentResolvers(resolvers);
        assertThat(resolvers).hasSize(2).first().isEqualTo(resolver);
        assertThat(resolvers).last()
                .asInstanceOf(InstanceOfAssertFactories.type(MergedIntentArgumentResolver.class))
                .satisfies(it -> {
                    assertThat(it.staticPermissionService).isEqualTo(staticPermissionService);
                    assertThat(it.jsonNodeHelper).isInstanceOf(JsonNodeHelperImpl.class);
                    assertThat(it.objectMapper).isEqualTo(objectMapper);
                });
    }

}
