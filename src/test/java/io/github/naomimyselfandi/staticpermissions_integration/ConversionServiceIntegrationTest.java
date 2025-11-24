package io.github.naomimyselfandi.staticpermissions_integration;

import io.github.naomimyselfandi.staticpermissions.EnableStaticPermissions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
class ConversionServiceIntegrationTest {

    @Autowired
    private ConversionService conversionService;

    @Test
    void conversionService() {
        var eggs = ThreadLocalRandom.current().nextInt();
        assertThat(conversionService.convert(eggs, FooIntent.class))
                .asInstanceOf(InstanceOfAssertFactories.type(FooIntent.class))
                .returns(eggs, FooIntent::getEggs)
                .returns(false, FooIntent::isSpam);
    }

}
