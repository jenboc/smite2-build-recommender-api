package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;

@Service 
public class GodStatTypeRetriever implements Retriever<GodStatType> {

    private final DataLoader dataLoader;
    private final IndexContainer indexContainer;

    public GodStatTypeRetriever(DataLoader dataLoader, IndexContainer indexContainer) {
        this.dataLoader = dataLoader;
        this.indexContainer = indexContainer;
    }

    /**
     * Retrieves chunks of items which provide a given stat
     * @param stat the stat to look for
     * @returns list of items providing stat
     */
    public List<IndexedChunk> retrieve(GodStatType stat) {
        if (indexContainer.getChunks().isEmpty()) return List.of();

        List<Item> items = dataLoader.getAllItems().stream()
            .filter(i -> i.getStats().containsKey(stat))
            .toList();

        return itemsToChunks(items);
    }

    private List<IndexedChunk> itemsToChunks(List<Item> items) {
        Set<String> itemNames = items.stream()
            .map(i -> i.getName().toLowerCase())
            .collect(Collectors.toSet());

        return indexContainer.getChunks().stream()
            .filter(c -> "item".equals(c.metadata().get("type"))
                    && itemNames.contains(c.metadata().get("name").toLowerCase()))
            .toList();
    }
}
