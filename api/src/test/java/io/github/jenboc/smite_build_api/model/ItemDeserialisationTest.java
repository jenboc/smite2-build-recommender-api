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
 
/**
 * Deserialises real scraped item JSON files, same rationale as
 * GodDeserialisationTest -- catches mismatches between actual scraper
 * output and what Item/ValueUnit/Unit expect, that a hand-written fixture
 * could accidentally paper over.
 *
 * Fixtures live under src/test/resources/items/. Two are used deliberately,
 * to cover both shapes an item can take:
 *   - gauntlet_of_thebes.json: has a passive_effect, no active_effect
 *   - (pick a second item that HAS an active_effect, e.g. a relic-adjacent
 *     or actively-used item) -- swap the filename below once you've picked one
 *
 * Copy real files from data/items/ here whenever the scraper's item output
 * shape changes, so this test keeps testing against current reality.
 */
@SpringBootTest
class ItemDeserialisationTest {
 
    @Autowired
    private JsonMapper jsonMapper;
 
    private Item load(String filename) throws IOException {
        try (InputStream in = new ClassPathResource("items/" + filename).getInputStream()) {
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
        // adjust this assertion if the scraper actually emits "" rather than
        // null for an absent active effect -- confirm against the real
        // scraped file before trusting either form
        assertTrue(item.getActiveEffect() == null || item.getActiveEffect().isBlank());
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

