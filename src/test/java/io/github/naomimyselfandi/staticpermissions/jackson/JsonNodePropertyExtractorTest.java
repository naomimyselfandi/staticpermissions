package io.github.naomimyselfandi.staticpermissions.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodePropertyExtractorTest {

    private ObjectMapper objectMapper;
    private JsonNodePropertyExtractor fixture;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        fixture = new JsonNodePropertyExtractor(objectMapper);
    }

    @Test
    void extract() throws Exception {
        interface Holder {int method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"a\": 5}");
        assertThat(fixture.extract(node, method, "a")).isEqualTo(5);
    }

    @Test
    void extract_WhenThePropertyIsOmitted_ThenReturnsNull() throws Exception {
        interface Holder {int method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"present\": 1}");
        assertThat(fixture.extract(node, method, "missing")).isNull();
    }

    @Test
    void extract_WhenThePropertyIsExplicitlyNull_ThenReturnsNull() throws Exception {
        interface Holder {int method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"a\": null}");
        assertThat(fixture.extract(node, method, "a")).isNull();
    }

    @Test
    void extract_WhenThePropertyIsMissing_ThenReturnsNull() throws Exception {
        interface Holder {int method();}
        var method = Holder.class.getMethod("method");
        var node = new ObjectNode(JsonNodeFactory.instance, Map.of("missing", MissingNode.getInstance()));
        assertThat(fixture.extract(node, method, "missing")).isNull();
    }

    @Test
    void extract_WhenThePropertyIsTheWrongType_ThenReturnsTypeMismatch() throws Exception {
        interface Holder {int method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"a\": \"not-a-number\"}");
        assertThat(fixture.extract(node, method, "a")).isEqualTo(PropertyExtractor.TYPE_MISMATCH);
    }

    @Test
    void extract_List() throws Exception {
        interface Holder {List<Integer> method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"numbers\": [1,2,3]}");
        assertThat(fixture.extract(node, method, "numbers")).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    void extract_Map() throws Exception {
        interface Holder {Map<String, String> method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"m\": {\"a\": \"x\", \"b\": \"y\"}}");
        assertThat(fixture.extract(node, method, "m"))
                .isEqualTo(Map.of("a", "x", "b", "y"));
    }

    @Test
    void extract_NestedGenerics() throws Exception {
        interface Holder {Set<Set<Integer>> method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"x\": [[1],[2]]}");
        assertThat(fixture.extract(node, method, "x")).isEqualTo(Set.of(Set.of(1), Set.of(2)));
    }

    @Test
    void extract_WhenAConversionFailureOccursDeepInTheTree_ThenReturnsTypeMismatch() throws Exception {
        interface Holder {List<Integer> method();}
        var method = Holder.class.getMethod("method");
        var node = objectMapper.readTree("{\"numbers\": [1, 2, \"not 3\", 4]}");
        assertThat(fixture.extract(node, method, "numbers"))
                .isEqualTo(PropertyExtractor.TYPE_MISMATCH);
    }

}
