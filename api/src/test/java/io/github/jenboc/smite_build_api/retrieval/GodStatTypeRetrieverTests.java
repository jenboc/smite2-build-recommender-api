package io.github.jenboc.smite_build_api.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.model.ValueUnit;

@ExtendWith(MockitoExtension.class)
class GodStatTypeRetrieverTests {

    @Mock private DataLoader dataLoader;
    @Mock private IndexContainer indexContainer;

    private GodStatTypeRetriever retriever;

    @BeforeEach
    void setUp() {
        retriever = new GodStatTypeRetriever(dataLoader, indexContainer);
    }


    private Item item(String name, GodStatType... stats) {
        Item item = new Item();
        item.setName(name);
        Map<GodStatType, ValueUnit> statMap = new java.util.HashMap<>();
        for (GodStatType stat : stats) {
            statMap.put(stat, new ValueUnit(10.0, ValueUnit.Unit.FLAT));
        }
        item.setStats(statMap);
        return item;
    }

    private IndexedChunk itemChunk(String name) {
        return new IndexedChunk(name + " chunk text", Map.of("type", "item", "name", name), List.of(1.0));
    }

    @Test
    void returnsChunksForItemsThatHaveTheStat() {
        Item gauntlet = item("Gauntlet of Thebes", GodStatType.MAX_HEALTH, GodStatType.PHYSICAL_PROTECTION);
        Item thebes2 = item("Sovereignty", GodStatType.MAX_HEALTH);

        when(dataLoader.getAllItems()).thenReturn(List.of(gauntlet, thebes2));
        when(indexContainer.getChunks()).thenReturn(List.of(
                itemChunk("Gauntlet of Thebes"),
                itemChunk("Sovereignty")
        ));

        List<IndexedChunk> result = retriever.retrieve(GodStatType.MAX_HEALTH);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> "Gauntlet of Thebes".equals(c.metadata().get("name"))));
        assertTrue(result.stream().anyMatch(c -> "Sovereignty".equals(c.metadata().get("name"))));
    }

    @Test
    void excludesItemsThatDoNotHaveTheStat() {
        Item withStat = item("Gauntlet of Thebes", GodStatType.MAX_HEALTH);
        Item withoutStat = item("Obsidian Shard", GodStatType.STRENGTH);

        when(dataLoader.getAllItems()).thenReturn(List.of(withStat, withoutStat));
        when(indexContainer.getChunks()).thenReturn(List.of(
                itemChunk("Gauntlet of Thebes"),
                itemChunk("Obsidian Shard")
        ));

        List<IndexedChunk> result = retriever.retrieve(GodStatType.MAX_HEALTH);

        assertEquals(1, result.size());
        assertEquals("Gauntlet of Thebes", result.get(0).metadata().get("name"));
    }

    @Test
    void excludesNonItemChunksEvenIfNameMatches() {
        // e.g. a god chunk happening to share text with an item's name --
        // only "item"-typed chunks should ever be returned
        Item withStat = item("Gauntlet of Thebes", GodStatType.MAX_HEALTH);

        when(dataLoader.getAllItems()).thenReturn(List.of(withStat));
        when(indexContainer.getChunks()).thenReturn(List.of(
                new IndexedChunk("not an item", Map.of("type", "ability", "name", "Gauntlet of Thebes"), List.of(1.0)),
                itemChunk("Gauntlet of Thebes")
        ));

        List<IndexedChunk> result = retriever.retrieve(GodStatType.MAX_HEALTH);

        assertEquals(1, result.size());
        assertEquals("item", result.get(0).metadata().get("type"));
    }

    @Test
    void returnsEmptyListWhenIndexIsEmpty() {
        when(indexContainer.getChunks()).thenReturn(List.of());

        List<IndexedChunk> result = retriever.retrieve(GodStatType.MAX_HEALTH);

        assertEquals(List.of(), result);
    }

    @Test
    void returnsEmptyListWhenNoItemHasTheStat() {
        Item withOtherStat = item("Obsidian Shard", GodStatType.STRENGTH);

        when(dataLoader.getAllItems()).thenReturn(List.of(withOtherStat));
        when(indexContainer.getChunks()).thenReturn(List.of(itemChunk("Obsidian Shard")));

        List<IndexedChunk> result = retriever.retrieve(GodStatType.MAX_HEALTH);

        assertTrue(result.isEmpty());
    }
}
