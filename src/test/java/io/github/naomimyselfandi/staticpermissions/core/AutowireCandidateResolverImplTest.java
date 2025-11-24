package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.IntentFactory;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.core.ResolvableType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutowireCandidateResolverImplTest {

    private interface Source {}
    private interface Target extends Intent {}

    @Mock
    private DependencyDescriptor descriptor;

    @Mock
    private StaticPermissionService staticPermissionService;

    @Mock
    private AutowireCandidateResolver delegate;

    @Mock
    private BeanFactory beanFactory;

    @InjectMocks
    private AutowireCandidateResolverImpl fixture;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getLazyResolutionProxyIfNecessary(boolean valid) {
        var type = ResolvableType.forClassWithGenerics(IntentFactory.class, Source.class, Target.class);
        when(beanFactory.getBean(StaticPermissionService.class)).thenReturn(staticPermissionService);
        when(staticPermissionService.isSourceFor(Source.class, Target.class)).thenReturn(valid);
        when(descriptor.getResolvableType()).thenReturn(type);
        if (valid) {
            assertThat(fixture.getLazyResolutionProxyIfNecessary(descriptor, UUID.randomUUID().toString()))
                    .isInstanceOfSatisfying(IntentFactoryImpl.class, it -> {
                        assertThat(it.type).isEqualTo(Target.class);
                        assertThat(it.staticPermissionService).isEqualTo(staticPermissionService);
                    });
        } else {
            assertThat(fixture.getLazyResolutionProxyIfNecessary(descriptor, UUID.randomUUID().toString())).isNull();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getLazyResolutionProxyIfNecessary_WhenTheDelegateHasABean_ThenUsesIt(boolean requestedFactory) {
        if (requestedFactory) {
            var type = ResolvableType.forClassWithGenerics(IntentFactory.class, Source.class, Target.class);
            lenient().when(staticPermissionService.isSourceFor(Source.class, Target.class)).thenReturn(true);
            lenient().when(descriptor.getResolvableType()).thenReturn(type);
        }
        var name = UUID.randomUUID().toString();
        var bean = new Object();
        when(delegate.getLazyResolutionProxyIfNecessary(descriptor, name)).thenReturn(bean);
        assertThat(fixture.getLazyResolutionProxyIfNecessary(descriptor, name)).isEqualTo(bean);
    }

    @Test
    void getLazyResolutionProxyIfNecessary_WhenTheRequestedTypeIsNotAFactory_ThenDoesNothing() {
        var name = UUID.randomUUID().toString();
        when(descriptor.getResolvableType()).thenReturn(ResolvableType.forType(Source.class));
        assertThat(fixture.getLazyResolutionProxyIfNecessary(descriptor, name)).isNull();
    }

}
