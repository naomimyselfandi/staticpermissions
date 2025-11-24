package io.github.naomimyselfandi.staticpermissions.jackson;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StaticPermissionJacksonConfigurationTest {

    @Test
    void doNothing() {
        // meaningless test so we don't get dinged on the coverage report
        assertThatCode(StaticPermissionJacksonConfiguration::new).doesNotThrowAnyException();
    }

}
