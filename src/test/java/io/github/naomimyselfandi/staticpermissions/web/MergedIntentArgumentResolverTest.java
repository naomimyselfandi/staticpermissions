package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.github.naomimyselfandi.staticpermissions.Intent;
import io.github.naomimyselfandi.staticpermissions.StaticPermissionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@ExtendWith(MockitoExtension.class)
class MergedIntentArgumentResolverTest {

    private interface TestIntent extends Intent {}

    @Mock
    private TestIntent intent;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private StaticPermissionService staticPermissionService;

    @Mock
    private JsonNodeHelper jsonNodeHelper;

    private MergedIntentArgumentResolver fixture;

    @BeforeEach
    void setup() {
        // var cache = new HashMap<String, TextNode>();
        lenient().when(jsonNodeHelper.infer(any())).then(invocation -> {
            var string = invocation.<String>getArgument(0);
            return string == null ? new TextNode("null") : new TextNode(string.toUpperCase());
            // return cache.computeIfAbsent(string, ignored -> new TextNode(UUID.randomUUID().toString()));
        });
        lenient().when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(request);
        fixture = new MergedIntentArgumentResolver(staticPermissionService, jsonNodeHelper, new ObjectMapper());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,true,true
            true,false,false
            false,true,false
            """)
    void supportsParameter(boolean annotated, boolean correctType, boolean expected) {
        var parameter = mock(MethodParameter.class);
        lenient().when(parameter.hasParameterAnnotation(MergedIntent.class)).thenReturn(annotated);
        lenient().when((Object) parameter.getParameterType()).thenReturn(TestIntent.class);
        lenient().when(staticPermissionService.isSourceFor(JsonNode.class, TestIntent.class)).thenReturn(correctType);
        assertThat(fixture.supportsParameter(parameter)).isEqualTo(expected);
    }

    @Test
    void resolveArgument() throws Exception {
        interface Holder {
            void holder(@MergedIntent(
                    path = {"a", "b:bb"},
                    params = {"c", "*d", "e:ee", "*f:ff"},
                    headers = {"g", "*h", "i:ii", "*j:jj"},
                    cookies = {"k:kk"}
            ) TestIntent parameter);
        }
        var parameter = new MethodParameter(Holder.class.getMethod("holder", TestIntent.class), 0);
        var a = UUID.randomUUID().toString();
        var b = UUID.randomUUID().toString();
        var c = UUID.randomUUID().toString();
        var d = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        var e = UUID.randomUUID().toString();
        var f = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        var g = UUID.randomUUID().toString();
        var h = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        var i = UUID.randomUUID().toString();
        var j = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        var k = UUID.randomUUID().toString();
        when(webRequest.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
                .thenReturn(Map.of("a", a, "bb", b, "unusedVariable", UUID.randomUUID().toString()));
        when(webRequest.getParameter("c")).thenReturn(c);
        when(webRequest.getParameterValues("d")).thenReturn(d);
        when(webRequest.getParameter("ee")).thenReturn(e);
        when(webRequest.getParameterValues("ff")).thenReturn(f);
        when(webRequest.getHeader("g")).thenReturn(g);
        when(webRequest.getHeaderValues("h")).thenReturn(h);
        when(webRequest.getHeader("ii")).thenReturn(i);
        when(webRequest.getHeaderValues("jj")).thenReturn(j);
        var cookie = new Cookie("kk", k);
        var unusedCookie = new Cookie("x", "y");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie, unusedCookie});
        var data = new ObjectNode(JsonNodeFactory.instance, Map.ofEntries(
                Map.entry("a", node(a)),
                Map.entry("b", node(b)),
                Map.entry("c", node(c)),
                Map.entry("d", node(d)),
                Map.entry("e", node(e)),
                Map.entry("f", node(f)),
                Map.entry("g", node(g)),
                Map.entry("h", node(h)),
                Map.entry("i", node(i)),
                Map.entry("j", node(j)),
                Map.entry("k", node(k))
        ));
        when(staticPermissionService.require(data, TestIntent.class)).thenReturn(intent);
        assertThat(fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo(intent);
        verifyNoInteractions(mavContainer, binderFactory);
        verify(request, never()).getInputStream();
    }

    @Test
    void resolveArgument_WhenTheBodyIsRequested_ThenProvidesIt() throws Exception {
        interface Holder {
            void holder(@MergedIntent(body = "bar") TestIntent parameter);
        }
        var parameter = new MethodParameter(Holder.class.getMethod("holder", TestIntent.class), 0);
        var foo = UUID.randomUUID().toString();
        var bar = UUID.randomUUID().toString();
        when(webRequest.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
                .thenReturn(Map.of("foo", foo));
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("""
                "%s"
                """.formatted(bar).getBytes())));
        when(request.getCookies()).thenReturn(null);
        var map = Map.of("foo", node(foo), "bar", new TextNode(bar));
        var data = new ObjectNode(JsonNodeFactory.instance, map);
        when(staticPermissionService.require(data, TestIntent.class)).thenReturn(intent);
        assertThat(fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo(intent);
        verifyNoInteractions(mavContainer, binderFactory);
    }

    @Test
    void resolveArgument_WhenTheBodyIsSplatted_ThenProvidesIt() throws Exception {
        interface Holder {
            void holder(@MergedIntent(body = "*") TestIntent parameter);
        }
        var parameter = new MethodParameter(Holder.class.getMethod("holder", TestIntent.class), 0);
        var foo = UUID.randomUUID().toString();
        var bar = UUID.randomUUID().toString();
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("""
                {
                  "foo": "%s",
                  "bar": "%s"
                }
                """.formatted(foo, bar).getBytes())));
        when(request.getCookies()).thenReturn(null);
        var map = Map.<String, JsonNode>of("foo", new TextNode(foo), "bar", new TextNode(bar));
        var data = new ObjectNode(JsonNodeFactory.instance, map);
        when(staticPermissionService.require(data, TestIntent.class)).thenReturn(intent);
        assertThat(fixture.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo(intent);
        verifyNoInteractions(mavContainer, binderFactory);
    }

    private JsonNode node(String string) {
        return jsonNodeHelper.infer(string);
    }

    private JsonNode node(String... strings) {
        return new ArrayNode(JsonNodeFactory.instance, Arrays.stream(strings).map(jsonNodeHelper::infer).toList());
    }

}
