package io.github.jenboc.smite_build_api.chunking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.model.ValueUnit;

class ItemChunkerTests {

    private final ItemChunker chunker = new ItemChunker();

    // Builds a fully-populated item based off of the Gauntlet of Thebes
    // Except with a made up active effect
    private Item fullItem() {
        Item item = new Item();

        item.setName("Gauntlet of Thebes");
        item.setTier(3);
        item.setCategory(Item.Category.DEFENSIVE);
        item.setCost(1400);
        item.setTotalCost(2200);
        item.setStats(Map.of(
            GodStatType.PHYSICAL_PROTECTION, new ValueUnit(25.0, ValueUnit.Unit.FLAT),
            GodStatType.MAX_HEALTH, new ValueUnit(200.0, ValueUnit.Unit.FLAT)
        ));
        item.setPassiveEffect("Assists on a minion give 1 Stack of Growth");
        item.setActiveEffect("Consume all stacks to heal for 10 per stack");
        item.setNotes(List.of(
            "At max stacks, this item provides 200 max health"
        ));

        return item;
    }

    @Test
    void includesTierInHeaderWhenPresent() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.startsWith("Gauntlet of Thebes (Tier 3 Defensive)"),
                "expected tier in header, but got: " + text);
    }

    @Test
    void omitsTierInHeaderWhenAbsent() {
        Item item = fullItem();
        item.setTier(null);

        String text = chunker.chunk(item).text();

        assertTrue(text.startsWith("Gauntlet of Thebes (Defensive)"),
                "expected no tier segment, but got: " + text);
        assertFalse(text.contains("Tier"));
    }

    @Test
    void showsCostAndTotalCost() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.contains("Cost: 1400 (2200 total)"),
                "expected cost and total cost, but got: " + text);
    }

    @Test
    void formatsWholeNumbersWithoutDecimalPoint() {
        String text = chunker.chunk(fullItem()).text();

        assertFalse(text.contains(".0"),
                "expected no trailing .0s on whole numbers, but got: " + text);
    }

    @Test
    void includesStatsLineWhenStatsPresent() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.contains("Stats:"),
                "expected stats to be included, but got: " + text);

        assertTrue(text.contains("25 Physical Protection"),
                "expected 25 Physical Protection, but got: " + text);

        assertTrue(text.contains("200 Max Health"),
                "expected 200 Max Health, but got: " + text);
    }

    @Test
    void omitsStatsLineWhenStatsAbsent() {
        Item item = fullItem();

        item.setStats(null);
        String nullText = chunker.chunk(item).text();

        item.setStats(Map.of());
        String emptyText = chunker.chunk(item).text();

        assertFalse(nullText.contains("Stats:"),
                "did not expect stats to be included when null, but got: " + nullText);

        assertFalse(emptyText.contains("Stats:"),
                "did not expect stats to be included when empty, but got: " + emptyText);
    }

    @Test
    void includesPassiveLineWhenPresent() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.contains("Passive: Assists on a minion give 1 Stack of Growth"),
                "expected passive effect to be included, but got: " + text);
    }

    @Test
    void omitsPassiveLineWhenAbsent() {
        Item item = fullItem();
        item.setPassiveEffect(null);

        String text = chunker.chunk(item).text();

        assertFalse(text.contains("Passive:"),
                "did not expect an included passive effect, but got: " + text);
    }

    @Test
    void includesActiveLineWhenPresent() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.contains("Active: Consume all stacks to heal for 10 per stack"),
                "expected active effect to be included, but got: " + text);
    }

    @Test
    void omitsActiveLineWhenAbsent() {
        Item item = fullItem();
        item.setActiveEffect(null);

        String text = chunker.chunk(item).text();

        assertFalse(text.contains("Active:"),
                "did not expect an included active effect, but got " + text);
    }

    @Test
    void includesNotesLineWhenPresent() {
        String text = chunker.chunk(fullItem()).text();

        assertTrue(text.contains("Notes: At max stacks, this item provides 200 max health"),
                "expected notes to be included, but got: " + text);
    }

    @Test
    void omitsNotesLineWhenAbsent() {
        Item item = fullItem();

        item.setNotes(null);
        String text = chunker.chunk(item).text();

        assertFalse(text.contains("Notes:"),
                "did not expect notes to be included, but got: " + text);

        item.setNotes(List.of());
        text = chunker.chunk(item).text();

        assertFalse(text.contains("Notes:"),
                "did not expect notes to be included, but got: " + text);
    }

    @Test
    void producesNoTrailingBlankLines() {
        String text = chunker.chunk(fullItem()).text();

        assertEquals(text.strip(), text);
    }

    @Test
    void metadataContainsTypeNameAndCategory() {
        Chunk chunk = chunker.chunk(fullItem());

        assertEquals("item", chunk.metadata().get("type"));
        assertEquals("Gauntlet of Thebes", chunk.metadata().get("name"));
        assertEquals("Defensive", chunk.metadata().get("category"));
    }
}
