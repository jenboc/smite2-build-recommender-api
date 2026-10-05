package io.github.jenboc.smite_build_api.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

/*
 * Test JSON deserialisation against a real JSON file to catch mismatches between
 * model expectations and the reality of the data produced from the scraper.
 *
 * The file lives at src/test/resources/gods/merlin.json -- a copy of a
 * real scraped file, picked because Merlin exercises stances and an
 * aspect in one file. Update this copy whenever the scraper's output
 * shape changes, so this test keeps testing against current reality.
 */
@SpringBootTest
class GodDeserialisationTest {

    @Autowired
    private JsonMapper jsonMapper;

    private God loadMerlin() throws IOException {
        try (InputStream in = new ClassPathResource("test-data/gods/merlin.json").getInputStream()) {
            return jsonMapper.readValue(in, God.class);
        }
    }

    @Test
    void deserialisesWithoutThrowing() {
        assertDoesNotThrow(this::loadMerlin);
    }

    @Test
    void topLevelFieldsArePopulated() throws IOException {
        God merlin = loadMerlin();

        assertEquals("Merlin", merlin.getName());
        assertNotNull(merlin.getRoles());
        assertFalse(merlin.getRoles().isEmpty());
        assertNotNull(merlin.getBaseStats());
        assertFalse(merlin.getBaseStats().isEmpty());
        assertNotNull(merlin.getAbilities());
    }

    @Test
    void stancesAreParsed() throws IOException {
        God merlin = loadMerlin();

        var stances = merlin.getAbilities().getStances();
        assertNotNull(stances);
        // Merlin specifically has Arcane/Fire/Ice -- a real regression here
        // (e.g. the stance-id lookup silently returning null, a bug this
        // project hit once already during scraping) would show up as a
        // missing or blank-keyed stance rather than an exception.
        assertFalse(stances.isEmpty());
        stances.keySet().forEach(name ->
                assertFalse(name == null || name.isBlank(), "found an invalid stance name: " + name));
    }

    @Test
    void aspectIsParsedWhenPresent() throws IOException {
        God merlin = loadMerlin();

        // Merlin has an aspect in the source data -- assert it actually came
        // through, rather than silently deserialising to null.
        assertNotNull(merlin.getAspect());
        assertNotNull(merlin.getAspect().getName());
        assertFalse(merlin.getAspect().getModifies() == null);
    }

    @Test
    void abilityStatsResolveToConcreteTypes() throws IOException {
        God merlin = loadMerlin();

        // Pull every AbilityStat out of every ability in every stance and
        // confirm the polymorphic StatData resolved to one of the three real
        // subclasses, not the empty base StatData -- that would mean the
        // @JsonTypeInfo/@JsonSubTypes wiring silently isn't working.
        merlin.getAbilities().getStances().values().stream()
                .flatMap(set -> java.util.stream.Stream.of(
                        set.getBasicAttack(), set.getPassive(), set.getFirst(),
                        set.getSecond(), set.getThird(), set.getUltimate()))
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.List::stream)
                .flatMap(ability -> ability.getStats().stream())
                .forEach(stat -> {
                    StatData data = stat.getData();
                    boolean isConcrete = data instanceof TieredStatData
                            || data instanceof ScaledStatData
                            || data instanceof TextStatData;
                    assertTrue(isConcrete,
                            "AbilityStat '" + stat.getName() + "' did not resolve to a concrete StatData subtype");
                });
    }
}
