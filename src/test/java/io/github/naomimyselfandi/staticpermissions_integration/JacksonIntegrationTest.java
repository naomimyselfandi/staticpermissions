package io.github.naomimyselfandi.staticpermissions_integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
class JacksonIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @CsvSource(textBlock = """
            '{"eggs":12}',12,false
            '{"eggs":5,"spam":false}',5,false
            '{"eggs":14,"spam":true}',14,true
            """)
    void canDeserializeIntentTypes(String json, int eggs, boolean spam) throws JsonProcessingException {
        assertThat(objectMapper.readValue(json, FooIntent.class))
                .returns(eggs, FooIntent::getEggs)
                .returns(spam, FooIntent::isSpam);
    }

}
