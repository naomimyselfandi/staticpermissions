package io.github.naomimyselfandi.staticpermissions_integration;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import io.github.naomimyselfandi.staticpermissions.IntentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
class FactoryIntegrationTest {

    @Autowired
    private IntentFactory<Integer, FooIntent> literalFactory;

    @Autowired
    private IntentFactory<String, FooIntent> convertingFactory;

    @Autowired
    private IntentFactory<FooRecord, FooIntent> adaptingFactory;

    @Autowired(required = false)
    private IntentFactory<Boolean, FooIntent> invalidFactory;

    @Autowired
    private AccessPolicy<FooIntent> accessPolicy;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testLiteralFactory(boolean deny) {
        var i = ThreadLocalRandom.current().nextInt();
        if (deny) {
            when(accessPolicy.apply(any())).thenReturn(RuntimeException::new);
            assertThat(literalFactory.request(i)).isEmpty();
        } else {
            when(accessPolicy.apply(any())).thenReturn(null);
            assertThat(literalFactory.request(i)).hasValueSatisfying(intent -> {
                assertThat(intent.getEggs()).isEqualTo(i);
                assertThat(intent.isSpam()).isFalse();
            });
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testConvertingFactory(boolean deny) {
        var i = ThreadLocalRandom.current().nextInt();
        if (deny) {
            when(accessPolicy.apply(any())).thenReturn(RuntimeException::new);
            assertThat(convertingFactory.request(String.valueOf(i))).isEmpty();
        } else {
            when(accessPolicy.apply(any())).thenReturn(null);
            assertThat(convertingFactory.request(String.valueOf(i))).hasValueSatisfying(intent -> {
                assertThat(intent.getEggs()).isEqualTo(i);
                assertThat(intent.isSpam()).isFalse();
            });
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testAdaptingFactory(boolean deny) {
        var i = ThreadLocalRandom.current().nextInt();
        var b = ThreadLocalRandom.current().nextBoolean();
        var r = new FooRecord(b, i);
        if (deny) {
            when(accessPolicy.apply(any())).thenReturn(RuntimeException::new);
            assertThat(adaptingFactory.request(r)).isEmpty();
        } else {
            when(accessPolicy.apply(any())).thenReturn(null);
            assertThat(adaptingFactory.request(r)).hasValueSatisfying(intent -> {
                assertThat(intent.getEggs()).isEqualTo(i);
                assertThat(intent.isSpam()).isEqualTo(b);
            });
        }
    }

    @Test
    void invalidFactoriesShouldNotBeProvided() {
        assertThat(invalidFactory).isNull();
    }

}
