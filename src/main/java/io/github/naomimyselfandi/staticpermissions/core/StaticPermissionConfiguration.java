package io.github.naomimyselfandi.staticpermissions.core;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;

@Configuration @ComponentScan
class StaticPermissionConfiguration {

    @Bean
    static BeanFactoryPostProcessor staticPermissionPostProcessor() {
        return beanFactory -> {
            if (beanFactory instanceof DefaultListableBeanFactory factory) {
                var delegate = factory.getAutowireCandidateResolver();
                var resolver = new AutowireCandidateResolverImpl(delegate, beanFactory);
                factory.setAutowireCandidateResolver(resolver);
            }
        };
    }

}
