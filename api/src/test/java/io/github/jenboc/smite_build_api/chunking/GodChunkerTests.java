package io.github.jenboc.smite_build_api.chunking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.jenboc.smite_build_api.model.Abilities;
import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.Aspect;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodBaseStat;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ScaledComponent;
import io.github.jenboc.smite_build_api.model.ScaledStatData;
import io.github.jenboc.smite_build_api.model.TextStatData;
import io.github.jenboc.smite_build_api.model.TieredStatData;
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
        god.setAbilities(new Abilities(Map.of(
            "Base", new AbilitySet(
                        // Basic Attack
                        List.of(
                            new Ability(
                                "Ability Name",
                                null,
                                List.of("Tag 1", "Tag 2"),
                                "Ability Description",
                                List.of(
                                    new AbilityStat(
                                        "Damage Scaling",
                                        new ScaledStatData(List.of(
                                            new ScaledComponent(
                                                new ValueUnit(50.0, ValueUnit.Unit.PERCENT),
                                                "Intelligence"
                                            ),
                                            new ScaledComponent(
                                                new ValueUnit(25.0, ValueUnit.Unit.PERCENT),
                                                "Strength"
                                            )
                                        ))
                                    ),
                                    new AbilityStat(
                                        "Damage",
                                        new TieredStatData(
                                            List.of(10.0, 20.0, 30.0, 40.0),
                                            "flat"
                                        )
                                    ),
                                    new AbilityStat(
                                        "Text Stat",
                                        new TextStatData("Text Stat Data")
                                    )
                                ),
                                List.of(
                                    "Ability Note 1.",
                                    "Ability Note 2."
                                )
                            )
                        ),
                        // Passive
                        List.of(),
                        // First Ability
                        List.of(),
                        // Second Ability
                        List.of(),
                        // Third Ability
                        List.of(),
                        // Ultimate
                        List.of()
                    )
        )));

        return god;
    }

    private Chunk overviewChunk(God god) {
        return chunker.chunk(god).get(0);
    }

    private Chunk abilityChunk(God god) {
        return chunker.chunk(god).get(1);
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
    void abilityChunkHeaderContainsGodSlotAndAbilityNames() {
        // Take Stance and Variant to be base/null (as it starts)
        String text = abilityChunk(fullGod()).text();

        assertTrue(text.startsWith("Merlin, Basic Attack - Ability Name"),
                "got: " + text);
    }

    @Test
    void abilityChunkHeaderContainsStanceIfNotBase() {
        // Take Variant to be null but Stance to be non-null and non-base
        God god = fullGod();
        god.getAbilities().setStances(Map.of(
            "Fire", god.getAbilities().getStances().get("Base")
        ));

        String text = abilityChunk(god).text();

        assertTrue(text.startsWith("Merlin, Fire Stance, Basic Attack - Ability Name"),
                "got: " + text);
    }

    @Test
    void abilityChunkHeaderContainsVariantIfNotNull() {
        // Take Variant to be non-null but Stance to be base
        God god = fullGod();
        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setVariant("Ability Variant");

        String text = abilityChunk(god).text();

        assertTrue(text.startsWith("Merlin, Basic Attack - Ability Name (Ability Variant)"),
                "got: " + text);
    }

    @Test
    void abilityChunkHeaderContainsStanceAndVariantIfBothPresent() {
        // Both Variant and Stance are non-null and non-base
        God god = fullGod();
        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setVariant("Ability Variant");
        god.getAbilities().setStances(Map.of(
            "Fire", god.getAbilities().getStances().get("Base")
        ));

        String text = abilityChunk(god).text();
        assertTrue(text.startsWith("Merlin, Fire Stance, Basic Attack - Ability Name (Ability Variant)"),
                "got: " + text);

    }

    @Test
    void abilityChunkContainsTagsIfPresentIfPresent() {
        String text = abilityChunk(fullGod()).text();
        assertTrue(text.contains("Tags: Tag 1, Tag 2"),
                "got: " + text);
    }

    @Test
    void abilityChunkOmitsTagsIfNotPresentIfPresent() {
        God god = fullGod();
        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setTags(List.of());

        String text = abilityChunk(god).text();
        assertFalse(text.contains("Tags:"), "got: " + text);

        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setTags(null);
        
        text = abilityChunk(god).text();
        assertFalse(text.contains("Tags:"), "got: " + text);
    }

    @Test
    void abilityChunkContainsTieredStatDataIfPresent() {
        String text = abilityChunk(fullGod()).text();
        assertTrue(text.contains("Damage: 10 | 20 | 30 | 40"),
                "got: " + text);
    }

    @Test
    void abilityChunkContainsScaledStatDataIfPresent() {
        String text = abilityChunk(fullGod()).text();
        assertTrue(text.contains("Damage Scaling: 50% Intelligence + 25% Strength"),
                "got: " + text);
    }

    @Test
    void abilityChunkContainsTextStatDataIfPresent() {
        String text = abilityChunk(fullGod()).text();
        assertTrue(text.contains("Text Stat: Text Stat Data"),
                "got: " + text);
    }

    @Test
    void abilityChunkContainsNotesIfPresent() {
        String text = abilityChunk(fullGod()).text();
        assertTrue(text.contains("Notes: Ability Note 1. Ability Note 2."),
                "got: " + text);
    }

    @Test
    void abilityChunkOmitsNotesIfNotPresent() {
        God god = fullGod();
        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setNotes(List.of());

        String text = abilityChunk(god).text();
        assertFalse(text.contains("Notes:"), "got: " + text);

        god.getAbilities().getStances().get("Base").getBasicAttack().get(0)
            .setNotes(null);
        
        text = abilityChunk(god).text();
        assertFalse(text.contains("Notes:"), "got: " + text);
    }

    @Test
    void abilityChunkMetadataContainsTypeNameGodStanceVariantAndSlot() {
        Map<String, String> meta = abilityChunk(fullGod()).metadata();

        assertEquals("ability", meta.get("type"));
        assertEquals("Merlin", meta.get("god"));
        assertEquals("Ability Name", meta.get("name"));
        assertEquals("Base", meta.get("stance"));
        assertEquals("Base", meta.get("variant"));
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
