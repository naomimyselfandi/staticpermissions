package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import io.github.naomimyselfandi.staticpermissions.Intent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticPermissionServiceImplTest {

    private static class Source {}
    private interface Target extends Intent {}

    private Source source;

    @Mock
    private ConfigurableConversionService conversionService;

    @Mock
    private AccessPolicy<Target> policy1, policy2;

    @Mock
    private Extractor<Source> extractor;

    @Mock
    private AccessPolicyRegistry accessPolicyRegistry;

    @Mock
    private ExtractorFactory extractorFactory;

    @Mock
    private NamingConvention namingConvention;

    @Mock
    private MethodRoleHelper methodRoleHelper;

    @Mock
    private ObjectProvider<ConfigurableConversionService> conversionServices;

    @InjectMocks
    private StaticPermissionServiceImpl fixture;

    @Mock
    private Authentication user;

    @BeforeEach
    void setup() {
        source = new Source();
        lenient().when(methodRoleHelper.getRole(any())).then(invocation -> {
            var method = invocation.<Method>getArgument(0);
            return switch (method.getName()) {
                case "equals" -> MethodRole.EQUALS;
                case "hashCode" -> MethodRole.HASH_CODE;
                case "toString" -> MethodRole.TO_STRING;
                default -> MethodRole.NON_PROPERTY;
            };
        });
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initialize() {
        when(conversionServices.iterator()).then(invocation -> List.of(conversionService).iterator());
        fixture.initialize();
        verify(conversionService).addConverter(new IntentConverter(fixture));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isSourceFor(boolean value) {
        when(extractorFactory.apply(Source.class, Target.class))
                .thenReturn(Optional.ofNullable(value ? extractor : null));
        assertThat(fixture.isSourceFor(Source.class, Target.class)).isEqualTo(value);
    }

    @Test
    void isSourceFor_WhenTheTargetIsNotAnIntentType_ThenFalse() {
        interface NotAnIntentType {}
        assertThat(fixture.isSourceFor(Source.class, NotAnIntentType.class)).isFalse();
    }

    @Test
    void isSourceFor_WhenTheTargetIsNotAnInterface_ThenFalse() {
        abstract class NotAnInterface implements Intent {}
        assertThat(fixture.isSourceFor(Source.class, NotAnInterface.class)).isFalse();
    }

    @Test
    void normalizeMethodName() throws NoSuchMethodException {
        interface Holder {
            Object someMethod();
        }
        var method = Holder.class.getMethod("someMethod");
        var normalized = UUID.randomUUID().toString();
        when(namingConvention.normalize("someMethod")).thenReturn(normalized);
        assertThat(fixture.normalizeMethodName(method)).isEqualTo(normalized);
    }

    @Test
    void require() {
        var intent = createIntent();
        assertThat(fixture.require(source, Target.class)).isEqualTo(intent);
    }

    @RepeatedTest(2)
    void require_WhenAPolicyDeniesAccess_ThenThrows(RepetitionInfo repetitionInfo) {
        var intent = createIntent();
        var exception = new RuntimeException();
        var problem = repetitionInfo.getCurrentRepetition() - 1;
        when(List.of(policy1, policy2).get(problem).apply(intent)).thenReturn(() -> exception);
        assertThatThrownBy(() -> fixture.require(source, Target.class)).isEqualTo(exception);
        if (problem == 0) {
            verify(policy2, never()).apply(intent);
        }
    }

    @Test
    void require_WhenTheSourceIsInvalid_ThenThrows() {
        when(extractorFactory.apply(Source.class, Target.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fixture.require(source, Target.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("%s is not a valid source for %s.", Source.class, Target.class);
    }

    @Test
    void request() {
        var intent = createIntent();
        assertThat(fixture.request(source, Target.class)).contains(intent);
    }

    @RepeatedTest(2)
    void require_WhenAPolicyDeniesAccess_ThenReturnsNothing(RepetitionInfo repetitionInfo) {
        var intent = createIntent();
        var exception = new RuntimeException();
        var problem = repetitionInfo.getCurrentRepetition() - 1;
        when(List.of(policy1, policy2).get(problem).apply(intent)).thenReturn(() -> exception);
        assertThat(fixture.request(source, Target.class)).isEmpty();
        if (problem == 0) {
            verify(policy2, never()).apply(intent);
        }
    }

    @Test
    void request_WhenTheSourceIsInvalid_ThenThrows() {
        when(extractorFactory.apply(Source.class, Target.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fixture.request(source, Target.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("%s is not a valid source for %s.", Source.class, Target.class);
    }

    private Target createIntent() {
        SecurityContextHolder.getContext().setAuthentication(user);
        when(extractorFactory.apply(Source.class, Target.class)).thenReturn(Optional.of(extractor));
        var values = Map.of(UUID.randomUUID().toString(), new Object());
        when(extractor.extract(source)).thenReturn(values);
        when((Object) accessPolicyRegistry.get(Target.class)).thenReturn(List.of(policy1, policy2));
        return new IntentInvocationHandler<>(Target.class, values, user, namingConvention, methodRoleHelper).get();
    }

}
