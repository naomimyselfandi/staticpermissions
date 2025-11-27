package io.github.naomimyselfandi.staticpermissions_integration;

import io.github.naomimyselfandi.staticpermissions.AccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestWebConfiguration.class)
class WebIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccessPolicy<FooIntent> accessPolicy;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).alwaysDo(print()).build();
        when(accessPolicy.apply(any())).thenReturn(null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canMergeIntents(boolean spam) throws Exception {
        var eggs = ThreadLocalRandom.current().nextInt();
        var content = mockMvc
                .perform(get("/test/ing/foo/{eggs}", eggs).queryParam("spam", String.valueOf(spam)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(content).isEqualTo("FooIntent[eggs=%d, spam=%s]", eggs, spam);
    }

    @Test
    void canUseDefaultsWhileMergeIntents() throws Exception {
        var eggs = ThreadLocalRandom.current().nextInt();
        var content = mockMvc
                .perform(get("/test/ing/foo/{eggs}", eggs))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(content).isEqualTo("FooIntent[eggs=%d]", eggs);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mergedIntentsImplyAccessChecks(boolean spam) throws Exception {
        var status = HttpStatus.valueOf(ThreadLocalRandom.current().nextInt(400, 420));
        when(accessPolicy.apply(any())).thenReturn(() -> new ResponseStatusException(status));
        var eggs = ThreadLocalRandom.current().nextInt();
         mockMvc.perform(get("/test/ing/foo/{eggs}", eggs).queryParam("spam", String.valueOf(spam)))
                .andExpect(status().is(status.value()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canUseBodyWhileMergingIntents(boolean spam) throws Exception {
        var eggs = ThreadLocalRandom.current().nextInt();
        var content = mockMvc
                .perform(get("/test/ing/bar/{eggs}", eggs).content(String.valueOf(spam)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(content).isEqualTo("FooIntent[eggs=%d, spam=%s]", eggs, spam);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void canUnwrapBodyWhileMergingIntents(boolean spam) throws Exception {
        var eggs = ThreadLocalRandom.current().nextInt();
        var content = mockMvc
                .perform(get("/test/ing/baz", eggs).content("""
                        {
                          "spam": %s,
                          "eggs": %d
                        }
                        """.formatted(spam, eggs)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(content).isEqualTo("FooIntent[eggs=%d, spam=%s]", eggs, spam);
    }

}
