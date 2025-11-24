package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentInvocationHandlerTest {

    private static final Object DEFAULT = new Object();
    private static final Object DEFAULT_2 = new Object();

    private interface TestIntent extends Intent {

        Object requiredProperty();

        Optional<Object> optionalProperty();

        default Object optionalPropertyWithDefault() {
            return DEFAULT;
        }

        Optional<Object> optionalPropertyWithOverride();

        default Object optionalPropertyWithOverriddenDefault() {
            return fail();
        }

        default Object defaultOnly() {
            return DEFAULT_2;
        }

    }

    private interface AnotherIntent extends Intent {}

    private Map<String, Object> values;

    private Object requiredPropertyValue, optionalPropertyValue, otherOptionalPropertyValue;

    @Mock
    private Authentication user, user2;

    @Mock
    private NamingConvention namingConvention;

    @Mock
    private MethodRoleHelperImpl methodRoleHelper;

    private IntentInvocationHandler<TestIntent> fixture;

    @BeforeEach
    void setup() {
        when(methodRoleHelper.getRole(any())).then(invocation -> {
            var name = invocation.<Method>getArgument(0).getName();
            return Arrays
                    .stream(MethodRole.values())
                    .filter(it -> switch (it) {
                        case EQUALS -> name.equals("equals");
                        case HASH_CODE -> name.equals("hashCode");
                        case TO_STRING -> name.equals("toString");
                        case AUTHENTICATION -> name.equals("__auth__");
                        case DATA_MAP -> name.equals("__data__");
                        case REQUIRED_PROPERTY -> name.toLowerCase().contains("required");
                        case OPTIONAL_PROPERTY -> name.toLowerCase().contains("optional");
                        case NON_PROPERTY -> true;
                    })
                    .findFirst()
                    .orElseThrow();
        });
        requiredPropertyValue = new Object();
        optionalPropertyValue = Optional.of(new Object());
        otherOptionalPropertyValue = new Object();
        values = Map.of(
                "requiredProperty", requiredPropertyValue,
                "optionalPropertyWithOverride", optionalPropertyValue,
                "optionalPropertyWithOverriddenDefault", otherOptionalPropertyValue,
                "defaultOnly", "this should not be used"
        );
        fixture = new IntentInvocationHandler<>(TestIntent.class, values, user, namingConvention, methodRoleHelper);
    }

    @Test
    void invoke_RequiredProperty() {
        assertThat(fixture.get().requiredProperty()).isEqualTo(requiredPropertyValue);
    }

    @Test
    void invoke_OptionalProperty() {
        assertThat(fixture.get().optionalProperty()).isEmpty();
    }

    @Test
    void invoke_OptionalPropertyWithDefault() {
        assertThat(fixture.get().optionalPropertyWithDefault()).isEqualTo(DEFAULT);
    }

    @Test
    void invoke_OptionalPropertyWithOverride() {
        assertThat(fixture.get().optionalPropertyWithOverride()).isEqualTo(optionalPropertyValue);
    }

    @Test
    void invoke_OptionalPropertyWithOverriddenDefault() {
        assertThat(fixture.get().optionalPropertyWithOverriddenDefault()).isEqualTo(otherOptionalPropertyValue);
    }

    @Test
    void invoke_DefaultOnly() {
        assertThat(fixture.get().defaultOnly()).isEqualTo(DEFAULT_2);
    }

    @Test
    void invoke_Equals() {
        var foo = new IntentInvocationHandler<>(TestIntent.class, Map.of(), user, namingConvention, methodRoleHelper);
        var bar = new IntentInvocationHandler<>(AnotherIntent.class, values, user, namingConvention, methodRoleHelper);
        var baz = new IntentInvocationHandler<>(TestIntent.class, values, user2, namingConvention, methodRoleHelper);
        assertThat(fixture.get())
                .isNotEqualTo(fixture)
                .isEqualTo(fixture.get())
                .isNotEqualTo(foo.get())
                .isNotEqualTo(bar.get())
                .isNotEqualTo(baz.get())
                .isNotEqualTo(null);
    }

    @Test
    void invoke_HashCode() {
        assertThat(fixture.get()).hasSameHashCodeAs(fixture);
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    void invoke_ToString() {
        when(namingConvention.normalize(any())).then(i -> i.<String>getArgument(0).toUpperCase());
        var proxy = fixture.get();
        assertThat(proxy).hasToString("TestIntent[%s=%s, %s=%s, %s=%s, %s=%s]",
                "DEFAULTONLY", "this should not be used",
                "OPTIONALPROPERTYWITHOVERRIDDENDEFAULT", otherOptionalPropertyValue,
                "OPTIONALPROPERTYWITHOVERRIDE", optionalPropertyValue,
                "REQUIREDPROPERTY", requiredPropertyValue
        );
    }

    @Test
    void invoke_Authentication() {
        assertThat(fixture.get().__auth__()).isEqualTo(user);
    }

    @Test
    void invoke_DataMap() {
        assertThat(fixture.get().__data__()).isEqualTo(values).isUnmodifiable();
    }

}
