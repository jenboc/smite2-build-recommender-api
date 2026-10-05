package io.github.jenboc.smite_build_api.model;
 
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;
 
/*
 * Tests JSON deserialisation of item files, following the same rationale as
 * GodDeserialisationTest
 *
 * Fixtures live under src/test/resources/items/. Two are used deliberately,
 * to cover both shapes an item can take:
 *   - gauntlet_of_thebes.json: has a passive_effect, no active_effect
 *   - aegis_of_acceleration.json: has a active_effect, no passive_effect
 */
@SpringBootTest
class ItemDeserialisationTest {
 
    @Autowired
    private JsonMapper jsonMapper;
 
    private Item load(String filename) throws IOException {
        try (InputStream in = new ClassPathResource("test-data/items/" + filename).getInputStream()) {
            return jsonMapper.readValue(in, Item.class);
        }
    }
 
    @ParameterizedTest
    @ValueSource(strings = {
        "gauntlet_of_thebes.json",
        "aegis_of_acceleration.json"
    })
    void deserialisesWithoutThrowing(String filename) {
        assertDoesNotThrow(() -> load(filename));
    }
 
    @ParameterizedTest
    @ValueSource(strings = {
        "gauntlet_of_thebes.json",
        "aegis_of_acceleration.json"
    })
    void coreFieldsArePopulated(String filename) throws IOException {
        Item item = load(filename);
 
        assertNotNull(item.getName());
        assertFalse(item.getName().isBlank());
        assertNotNull(item.getCategory());
        assertTrue(item.getCost() >= 0);
        assertTrue(item.getTotalCost() >= item.getCost(),
                "total_cost (" + item.getTotalCost() + ") should never be less than cost (" + item.getCost() + ")");
        assertNotNull(item.getStats());
        assertNotNull(item.getNotes());
    }
 
    @Test
    void gauntletOfThebesHasPassiveButNoActive() throws IOException {
        Item item = load("gauntlet_of_thebes.json");
 
        assertNotNull(item.getPassiveEffect());
        assertFalse(item.getPassiveEffect().isBlank());
        assertTrue(item.getActiveEffect() == null || item.getActiveEffect().isBlank());
    }

    @Test
    void aegisOfAccelerationHasActiveButNoPassive() throws IOException {
        Item item = load("aegis_of_acceleration.json");
 
        assertNotNull(item.getActiveEffect());
        assertFalse(item.getActiveEffect().isBlank());
        assertTrue(item.getPassiveEffect() == null || item.getPassiveEffect().isBlank());
    }
 
    @Test
    void statsMapKeysAndValuesResolveCorrectly() throws IOException {
        Item item = load("gauntlet_of_thebes.json");
 
        assertFalse(item.getStats().isEmpty(), "expected at least one stat on Gauntlet of Thebes");
        item.getStats().forEach((key, valueUnit) -> {
            assertNotNull(key);
            assertNotNull(valueUnit);
            assertNotNull(valueUnit.getUnit());
        });
    }
}

