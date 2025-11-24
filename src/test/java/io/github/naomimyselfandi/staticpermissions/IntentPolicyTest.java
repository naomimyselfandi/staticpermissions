package io.github.naomimyselfandi.staticpermissions;

import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import static org.assertj.core.api.Assertions.*;

class IntentPolicyTest {

    private interface TestIntent extends Intent {}

    @Test
    void getIntentType() {
        var accessPolicy = new AccessPolicy<TestIntent>() {

            @Override
            public Denial apply(@NonNull TestIntent intent) {
                return fail();
            }

        };
        assertThat(accessPolicy.getIntentType()).isEqualTo(TestIntent.class);
    }

}
