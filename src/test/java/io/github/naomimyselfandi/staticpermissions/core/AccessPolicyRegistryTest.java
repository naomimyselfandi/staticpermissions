package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import io.github.naomimyselfandi.staticpermissions.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessPolicyRegistryTest {

    private interface FooIntent extends Intent {}
    private interface BarIntent extends FooIntent {}
    private interface BazIntent extends BarIntent {}

    private AccessPolicyRegistry fixture;

    @Mock
    private AccessPolicy<FooIntent> policy1;

    @Mock
    private AccessPolicy<BarIntent> policy2;

    @Mock
    private AccessPolicy<BazIntent> policy3;

    @BeforeEach
    void setup() {
        when(policy1.getIntentType()).thenReturn(FooIntent.class);
        when(policy2.getIntentType()).thenReturn(BarIntent.class);
        when(policy3.getIntentType()).thenReturn(BazIntent.class);
        var policies = new ArrayList<>(List.of(policy1, policy2, policy3));
        Collections.shuffle(policies);
        fixture = new AccessPolicyRegistryImpl(List.copyOf(policies));
    }

    @RepeatedTest(4)
    void get() {
        assertThat(fixture.get(BarIntent.class)).isEqualTo(List.of(policy1, policy2));
    }

}
