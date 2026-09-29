package io.github.jenboc.smite_build_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jenboc.smite_build_api.model.GodStatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class JacksonConfigTests {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void deserialisesSnakeCase() {
        record Sample(String basicAttack) {}

        Sample s = jsonMapper.readValue(
            "{\"basic_attack\": \"test string\"}",
            Sample.class
        );

        assertEquals("test string", s.basicAttack());
    }

    @ParameterizedTest
    @CsvSource({
        "attack_damage, ATTACK_DAMAGE",
        "aTtAcK_spEeD, ATTACK_SPEED",
        "COOLDOWN_RATE, COOLDOWN_RATE",
        "critical_CHANCE, CRITICAL_CHANCE",
        "lifesteal, LIFESTEAL",
        "LIFESTEAL, LIFESTEAL"
    })
    void usesCaseInsensitiveEnums(String json, GodStatType expected) {
        GodStatType actual = jsonMapper.readValue(
            "\"" + json + "\"",
            GodStatType.class
        );
        
        assertEquals(expected, actual);
    }

    @Test
    void rejectsUnknownEnumValues() {
        record Sample(GodStatType type) {}

        assertThrows(
            RuntimeException.class,
            () -> jsonMapper.readValue(
                "{\"type\": \"asdlk;kasdk\"}",
                Sample.class
            )
        );
    }
}
