package io.github.naomimyselfandi.staticpermissions.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticPermissionConfigurationTest {

    @Mock
    private ConfigurableListableBeanFactory nonDefaultBeanFactory;

    private BeanFactoryPostProcessor fixture;

    @BeforeEach
    void setup() {
        fixture = StaticPermissionConfiguration.staticPermissionPostProcessor();
    }

    @Test
    void postProcessBeanFactory() {
        var beanFactory = new DefaultListableBeanFactory();
        var autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        fixture.postProcessBeanFactory(beanFactory);
        assertThat(beanFactory.getAutowireCandidateResolver())
                .isInstanceOfSatisfying(AutowireCandidateResolverImpl.class, it -> {
                    assertThat(it.delegate).isEqualTo(autowireCandidateResolver);
                    assertThat(it.beanFactory).isEqualTo(beanFactory);
                });
    }

    @Test
    void postProcessBeanFactory_WhenTheBeanFactoryIsAnUnexpectedType_ThenDoesNothing() {
        assertThatCode(() -> fixture.postProcessBeanFactory(nonDefaultBeanFactory)).doesNotThrowAnyException();
        verifyNoInteractions(nonDefaultBeanFactory);
    }

}
