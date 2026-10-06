package io.github.jenboc.smite_build_api.recommending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.retrieval.GodMentionRetriever;
import io.github.jenboc.smite_build_api.retrieval.GodStatTypeRetriever;
import io.github.jenboc.smite_build_api.retrieval.ItemRetriever;
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextGathererSynergyTests {

    @Mock
    private GodMentionRetriever godRetriever;

    @Mock
    private ItemRetriever itemRetriever;

    @Mock
    private GodStatTypeRetriever statRetriever;

    @Mock
    private VectorRetriever vecRetriever;

    private ContextGatherer contextGatherer;

    @BeforeEach
    void setUp() {
        contextGatherer = new ContextGatherer(
                godRetriever,
                itemRetriever,
                statRetriever,
                vecRetriever
        );
    }

    @Test
    void gathersWantedItems() {
        Item wantedItem = item("Spear");
        IndexedChunk wantedChunk = itemChunk("Spear", List.of(1.0, 2.0));

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(wantedChunk));

        when(vecRetriever.retrieve(wantedChunk.embedding(), 5))
                .thenReturn(List.of());

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(List.of(wantedItem), List.of())
        );

        assertTrue(result.contains(wantedChunk));
        verify(itemRetriever).retrieve(wantedItem);
    }

    @Test
    void retrievesRelatedItemsForEachWantedItem() {
        Item firstItem = item("Spear");
        Item secondItem = item("Shield");

        IndexedChunk firstChunk = itemChunk(
                "Spear",
                List.of(1.0, 2.0)
        );

        IndexedChunk secondChunk = itemChunk(
                "Shield",
                List.of(3.0, 4.0)
        );

        IndexedChunk firstRelated = chunk(
                "first related",
                List.of(5.0, 6.0)
        );

        IndexedChunk secondRelated = chunk(
                "second related",
                List.of(7.0, 8.0)
        );

        when(itemRetriever.retrieve(firstItem))
                .thenReturn(List.of(firstChunk));

        when(itemRetriever.retrieve(secondItem))
                .thenReturn(List.of(secondChunk));

        when(vecRetriever.retrieve(firstChunk.embedding(), 5))
                .thenReturn(List.of(firstRelated));

        when(vecRetriever.retrieve(secondChunk.embedding(), 5))
                .thenReturn(List.of(secondRelated));

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(
                        List.of(firstItem, secondItem),
                        List.of()
                )
        );

        assertTrue(result.contains(firstChunk));
        assertTrue(result.contains(secondChunk));
        assertTrue(result.contains(firstRelated));
        assertTrue(result.contains(secondRelated));

        verify(vecRetriever).retrieve(firstChunk.embedding(), 5);
        verify(vecRetriever).retrieve(secondChunk.embedding(), 5);
    }

    @Test
    void removesExcludedItems() {
        Item wantedItem = item("Spear");
        Item excludedItem = item("Shield");

        IndexedChunk wantedChunk = itemChunk(
                "Spear",
                List.of(1.0, 2.0)
        );

        IndexedChunk excludedChunk = itemChunk(
                "Shield",
                List.of(3.0, 4.0)
        );

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(wantedChunk));

        when(vecRetriever.retrieve(wantedChunk.embedding(), 5))
                .thenReturn(List.of(excludedChunk));

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(
                        List.of(wantedItem),
                        List.of(excludedItem)
                )
        );

        assertTrue(result.contains(wantedChunk));
        assertTrue(result.stream().noneMatch(excludedChunk::equals));
    }

    @Test
    void itemExclusionIsCaseInsensitive() {
        Item wantedItem = item("Spear");
        Item excludedItem = item("shield");

        IndexedChunk wantedChunk = itemChunk(
                "Spear",
                List.of(1.0, 2.0)
        );

        IndexedChunk relatedChunk = itemChunk(
                "SHIELD",
                List.of(3.0, 4.0)
        );

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(wantedChunk));

        when(vecRetriever.retrieve(wantedChunk.embedding(), 5))
                .thenReturn(List.of(relatedChunk));

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(
                        List.of(wantedItem),
                        List.of(excludedItem)
                )
        );

        assertTrue(result.contains(wantedChunk));
        assertTrue(result.stream().noneMatch(relatedChunk::equals));
    }

    @Test
    void retainsNonItemRelatedChunks() {
        Item wantedItem = item("Spear");

        IndexedChunk wantedChunk = itemChunk(
                "Spear",
                List.of(1.0, 2.0)
        );

        IndexedChunk relatedAbility = chunk(
                "related ability",
                List.of(3.0, 4.0)
        );

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(wantedChunk));

        when(vecRetriever.retrieve(wantedChunk.embedding(), 5))
                .thenReturn(List.of(relatedAbility));

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(List.of(wantedItem), List.of())
        );

        assertTrue(result.contains(wantedChunk));
        assertTrue(result.contains(relatedAbility));
    }

    @Test
    void deduplicatesIdenticalChunks() {
        Item wantedItem = item("Spear");

        IndexedChunk wantedChunk = itemChunk(
                "Spear",
                List.of(1.0, 2.0)
        );

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(wantedChunk));

        when(vecRetriever.retrieve(wantedChunk.embedding(), 5))
                .thenReturn(List.of(wantedChunk));

        List<IndexedChunk> result = contextGatherer.gather(
                synergyQuery(List.of(wantedItem), List.of())
        );

        assertTrue(result.contains(wantedChunk));
        assertTrue(result.stream()
                .filter(wantedChunk::equals)
                .count() == 1);
    }

    private static DecodedQuery synergyQuery(
            List<Item> wantedItems,
            List<Item> excludedItems
    ) {
        return new DecodedQuery(
                QueryType.ITEM_SYNERGY,
                List.of(),
                List.of(),
                wantedItems,
                excludedItems,
                List.of(),
                List.of(),
                "item synergy"
        );
    }

    private static Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    private static IndexedChunk chunk(
            String text,
            List<Double> embedding
    ) {
        return new IndexedChunk(
                text,
                Map.of("type", "ability", "name", text),
                embedding
        );
    }

    private static IndexedChunk itemChunk(
            String name,
            List<Double> embedding
    ) {
        return new IndexedChunk(
                name,
                Map.of("type", "item", "name", name),
                embedding
        );
    }
}
