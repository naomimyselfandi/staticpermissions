package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class StaticPermissionWebConfiguration implements WebMvcConfigurer {

    private final StaticPermissionService staticPermissionService;
    private final ObjectMapper objectMapper;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        var jsonNodeParser = new JsonNodeHelperImpl();
        resolvers.add(new MergedIntentArgumentResolver(staticPermissionService, jsonNodeParser, objectMapper));
    }

}
