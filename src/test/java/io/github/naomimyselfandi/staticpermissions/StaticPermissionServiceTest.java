package io.github.naomimyselfandi.staticpermissions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StaticPermissionServiceTest {

    private RuntimeException failure;

    private interface TestIntent extends Intent {}

    @Mock
    private Authentication authentication, originalAuthentication;

    private Object source;

    @Mock
    private TestIntent intent;

    private StaticPermissionService fixture;

    private boolean requireWasCalled;

    @BeforeEach
    void setup() {
        source = new Object();
        fixture = new StaticPermissionService() {

            @Override
            public boolean isSourceFor(@NonNull Class<?> source, @NonNull Class<?> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <I extends Intent> @NonNull I require(@NonNull Object source, @NonNull Class<I> type) {
                requireWasCalled = true;
                return request(source, type).orElseThrow();
            }

            @Override
            public <I extends Intent> @NonNull Optional<I> request(@NonNull Object source, @NonNull Class<I> type) {
                if (failure != null) {
                    throw failure;
                }
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
                assertThat(source).isEqualTo(StaticPermissionServiceTest.this.source);
                return Optional.of(type.cast(intent));
            }

            @Override
            public @NonNull String normalizeMethodName(@NonNull Method method) {
                throw new UnsupportedOperationException();
            }

        };
        SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
    }

    @AfterEach
    void teardown() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(originalAuthentication);
        SecurityContextHolder.clearContext();
    }

    @Test
    void require() {
        assertThat(fixture.require(source, TestIntent.class, authentication)).isEqualTo(intent);
        assertThat(requireWasCalled).isTrue();
    }

    @Test
    void require_WhenTheCallFails_ThenStillResetsTheContext() {
        failure = new RuntimeException();
        assertThatThrownBy(() -> fixture.require(source, TestIntent.class, authentication)).isEqualTo(failure);
    }

    @Test
    void request() {
        assertThat(fixture.request(source, TestIntent.class, authentication)).contains(intent);
        assertThat(requireWasCalled).isFalse();
    }

    @Test
    void request_WhenTheCallFails_ThenStillResetsTheContext() {
        failure = new RuntimeException();
        assertThatThrownBy(() -> fixture.request(source, TestIntent.class, authentication)).isEqualTo(failure);
    }

}
