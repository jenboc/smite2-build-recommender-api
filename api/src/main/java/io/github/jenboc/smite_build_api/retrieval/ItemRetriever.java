package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.model.Item;

/**
 * Retrieves chunks based off of Item objects
 * @see Item
 */
@Service
public class ItemRetriever implements Retriever<Item> {

    private final IndexContainer indexContainer;

    public ItemRetriever(IndexContainer indexContainer) {
        this.indexContainer = indexContainer;
    }

    public List<IndexedChunk> retrieve(Item item) {
        // Only the item chunks exist for items
        return indexContainer.getChunks().stream()
            .filter(c -> "item".equals(c.metadata().get("type"))
                    && item.getName().equalsIgnoreCase(c.metadata().get("name")))
            .toList();
    }
}
