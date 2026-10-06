package io.github.jenboc.smite_build_api.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.github.jenboc.smite_build_api.model.Item;

@ExtendWith(MockitoExtension.class)
class ItemRetrieverTests {

    @Mock private IndexContainer indexContainer;

    private ItemRetriever retriever;

    @BeforeEach 
    void setUp() {
        retriever = new ItemRetriever(indexContainer);
    }

    private Item item(String itemName) {
        Item item = new Item();
        item.setName(itemName);
        return item;
    }

    @Test
    void emptyItemContainerReturnsEmpty() {
        when(indexContainer.getChunks())
            .thenReturn(List.of());

        assertEquals(List.of(), retriever.retrieve(item("item")));
    }

    @Test
    void onlyReturnsItemsChunk() {
        String itemName = "the item of choice";
        List<IndexedChunk> chunks = List.of(
                new IndexedChunk("Correct", Map.of("type", "item", "name", itemName), List.of(1.0)),
                new IndexedChunk("Incorrect", Map.of("type", "item", "name", "nuh uh"), List.of(1.0)),
                new IndexedChunk("Same name, wrong type", Map.of("type", "wrong", "name", "itemName"), List.of(1.0))
        );
        
        when(indexContainer.getChunks())
            .thenReturn(chunks);

        assertEquals(List.of(chunks.get(0)), retriever.retrieve(item(itemName)));
    }
}
