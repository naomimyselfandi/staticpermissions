package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MethodRoleHelperImplTest {

    private MethodRoleHelper fixture;

    @BeforeEach
    void setup() {
        fixture = new MethodRoleHelperImpl();
    }

    @MethodSource
    @ParameterizedTest
    void getRole(Method method, MethodRole expected) {
        assertThat(fixture.getRole(method)).isEqualTo(expected);
    }

    @MethodSource
    @ParameterizedTest
    void getRole_WhenTheMethodIsInvalid_ThenThrows(Method method) {
        assertThatThrownBy(() -> fixture.getRole(method))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid intent type method '%s'.", method);
    }

    private static Stream<Arguments> getRole() throws NoSuchMethodException {
        @SuppressWarnings("unused")
        interface Holder {
            Object requiredProperty();
            Optional<Object> optionalProperty();
            default Object defaultProperty() {
                return fail();
            }
            default void voidMethod() {}
            default Object defaultMethod(Object something) {
                return fail();
            }
            static Object staticMethod(Object something) {
                return fail();
            }
            @Intent.NotProperty
            default Object notProperty() {
                return fail();
            }
        }
        return Stream.of(
                arguments(Object.class.getMethod("equals", Object.class), MethodRole.EQUALS),
                arguments(Object.class.getMethod("hashCode"), MethodRole.HASH_CODE),
                arguments(Object.class.getMethod("toString"), MethodRole.TO_STRING),
                arguments(Intent.class.getMethod("__auth__"), MethodRole.AUTHENTICATION),
                arguments(Intent.class.getMethod("__data__"), MethodRole.DATA_MAP),
                arguments(Holder.class.getMethod("requiredProperty"), MethodRole.REQUIRED_PROPERTY),
                arguments(Holder.class.getMethod("optionalProperty"), MethodRole.OPTIONAL_PROPERTY),
                arguments(Holder.class.getMethod("defaultProperty"), MethodRole.OPTIONAL_PROPERTY),
                arguments(Holder.class.getMethod("defaultMethod", Object.class), MethodRole.NON_PROPERTY),
                arguments(Holder.class.getMethod("voidMethod"), MethodRole.NON_PROPERTY),
                arguments(Holder.class.getMethod("staticMethod", Object.class), MethodRole.NON_PROPERTY),
                arguments(Holder.class.getMethod("notProperty"), MethodRole.NON_PROPERTY)
        );
    }

    private static Stream<Method> getRole_WhenTheMethodIsInvalid_ThenThrows() {
        @SuppressWarnings("unused")
        interface Helper {
            void voidMethod();
            Object methodWithUnexpectedProperty(Object something);
            Object __auth__(Object something);
            Object __data__(Object something);
            Object __unknownReservedMethod__();
        }
        return Arrays.stream(Helper.class.getMethods());
    }

}
