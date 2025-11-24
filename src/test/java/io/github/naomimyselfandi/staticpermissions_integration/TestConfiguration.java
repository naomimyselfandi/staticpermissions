package io.github.naomimyselfandi.staticpermissions_integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import io.github.naomimyselfandi.staticpermissions.EnableStaticPermissions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@EnableStaticPermissions
class TestConfiguration {

    private interface FooAccessPolicy extends AccessPolicy<FooIntent> {}

    @Bean
    AccessPolicy<FooIntent> accessPolicy() {
        var mock = mock(FooAccessPolicy.class);
        when(mock.getIntentType()).thenCallRealMethod();
        return mock;
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    ConversionService conversionService() {
        return new DefaultFormattingConversionService();
    }

}
