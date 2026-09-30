package io.github.jenboc.smite_build_api.chunking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.jenboc.smite_build_api.model.Aspect;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodBaseStat;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ValueUnit;

class GodChunkerTests {

    private final GodChunker chunker = new GodChunker();

    private God fullGod() {
        God god = new God();

        god.setName("Merlin");
        god.setRoles(List.of(God.Role.MID));
        god.setDamageType(God.DamageType.MAGICAL);
        god.setDamageRange(God.DamageRange.RANGED);
        god.setSpecialisations(List.of("Burst Damage", "Nuker"));
        god.setBaseStats(Map.of(
            GodStatType.MAX_HEALTH, new GodBaseStat(
                new ValueUnit(450.0, ValueUnit.Unit.FLAT),
                new ValueUnit(80.0, ValueUnit.Unit.FLAT)
            ),
            GodStatType.MAX_MANA, new GodBaseStat(
                new ValueUnit(220.0, ValueUnit.Unit.FLAT),
                new ValueUnit(40.0, ValueUnit.Unit.FLAT)
            )
        ));
        god.setAspect(new Aspect(
            "Aspect of Pandemonium",
            "Elemental Mastery has a reduced cooldown",
            Map.of()
        ));
        god.setAbilities(null);

        return god;
    }

    private Chunk overviewChunk(God god) {
        return chunker.chunk(god).get(0);
    }

    @Test
    void overviewHeaderIncludesNameAndRoles() {
        String text = overviewChunk(fullGod()).text();
        assertTrue(text.startsWith("Merlin - Mid"), "got: " + text);
    }

    @Test
    void overviewHeaderJoinsMultipleRoles() {
        God god = fullGod();
        god.setRoles(List.of(God.Role.MID, God.Role.SOLO));
        String text = overviewChunk(god).text();
        assertTrue(text.startsWith("Merlin - Mid, Solo"), "got: " + text);
    }

    @Test
    void overviewIncludesDamageInfo() {
        String text = overviewChunk(fullGod()).text();
        assertTrue(text.contains("Magical Ranged"));
    }

    @Test
    void overviewIncludesSpecialisationsWhenPresent() {
        String text = overviewChunk(fullGod()).text();
        assertTrue(text.contains("Specialisations: Burst Damage, Nuker"), "got: " + text);
    }

    @Test
    void overviewOmitsSpecialisationsWhenNullOrEmpty() {
        God god = fullGod();

        god.setSpecialisations(null);
        String nullText = overviewChunk(god).text();

        god.setSpecialisations(List.of());
        String emptyText = overviewChunk(god).text();

        assertFalse(nullText.contains("Specialisations:"), "when null, got: " + nullText);
        assertFalse(emptyText.contains("Specialisations:"), "when empty, got: " + emptyText);
    }

    @Test
    void overviewIncludesBaseStats() {
        String text = overviewChunk(fullGod()).text();

        assertTrue(text.contains("Base stats:"), "got: " + text);
        assertTrue(text.contains("450 (+80 per level) Max Health"), "got: " + text);
        assertTrue(text.contains("220 (+40 per level) Max Mana"), "got: " + text);
    }

    @Test
    void overviewIncludesAspectNameIfPresent() {
        String text = overviewChunk(fullGod()).text();
        assertTrue(text.contains("Aspect: Aspect of Pandemonium"), "got: " + text);
    }

    @Test
    void overviewOmitsAspectNameIfNull() {
        God god = fullGod();
        god.setAspect(null);

        String text = overviewChunk(god).text();
        assertFalse(text.contains("Aspect:"), "got: " + text);
    }

    @Test
    void overviewMetadataContainsTypeNameRolesAndDamageInfo() {
        Map<String, String> meta = overviewChunk(fullGod()).metadata();

        assertEquals("god", meta.get("type"));
        assertEquals("Merlin", meta.get("name"));
        assertEquals("Mid", meta.get("roles"));
        assertEquals("Magical", meta.get("damage_type"));
        assertEquals("Ranged", meta.get("damage_range"));
    }

    @Test 
    void producesFullyTrimmedText() {
        List<Chunk> chunks = chunker.chunk(fullGod());

        chunks.forEach(chunk -> {
            assertEquals(chunk.text().strip(), chunk.text(),
                    "expected chunk text to be trimmed, but got: " + chunk.text());
        });
    }
}
