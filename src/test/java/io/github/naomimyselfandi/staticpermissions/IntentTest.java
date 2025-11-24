package io.github.naomimyselfandi.staticpermissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentTest {

    @Mock
    private Authentication authentication;

    @Mock
    private Intent intent;

    @Test
    void getAuthentication() {
        when(intent.__auth__()).thenReturn(authentication);
        assertThat(Intent.getAuthentication(intent)).isEqualTo(authentication);
    }

}
