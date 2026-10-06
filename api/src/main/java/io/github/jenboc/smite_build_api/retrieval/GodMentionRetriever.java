package io.github.jenboc.smite_build_api.retrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

/**
 * Retrieves chunks based off of GodMention objects
 * @see GodMention
 */
@Service
public class GodMentionRetriever implements Retriever<GodMention> {

    private final IndexContainer indexContainer;

    public GodMentionRetriever(IndexContainer indexContainer) {
        this.indexContainer = indexContainer;
    }

    public List<IndexedChunk> retrieve(GodMention mention) {
        return aspectFiltering(
                allGodChunks(mention.god().getName()),
                mention.aspect()
        );
    }

    private List<IndexedChunk> allGodChunks(String godName) {
        return indexContainer.getChunks().stream()
            .filter(c -> godName.equalsIgnoreCase(c.metadata().get("name"))
                    || godName.equalsIgnoreCase(c.metadata().get("god")))
            .toList();
    }

    private List<IndexedChunk> aspectFiltering(List<IndexedChunk> godChunks, AspectFlag aspect) {
        // If we haven't specified w/, w/out aspect, return them all
        if (aspect == AspectFlag.UNSPECIFIED) {
            return godChunks;
        }

        List<IndexedChunk> filteredChunks = new ArrayList<>();
        Map<String, List<IndexedChunk>> abilitySlots = new HashMap<>();

        for (IndexedChunk chunk : godChunks) {
            // If it's an overview chunk, keep it
            if ("god".equals(chunk.metadata().get("type"))) {
                filteredChunks.add(chunk);
                continue;
            }

            // If it's an aspect chunk, only keep it if we're using an aspect
            if ("aspect".equals(chunk.metadata().get("type")) && aspect == AspectFlag.ASPECT) {
                filteredChunks.add(chunk);
                continue;
            }

            // We assume we only have ability chunks left.
            if (!"ability".equals(chunk.metadata().get("type"))) {
                continue;
            }

            // Ability slots should have a "slot" and "stance" in their metadata
            // We populate our abilitySlots map using the slots + stance as keys
            // We ensure every element in a list in this map, is either an aspect or not
            // i.e. an aspect and non-aspect ability CANNOT appear in the same list
            String key = chunk.metadata().get("slot") + " - " + chunk.metadata().get("stance");

            // Not in the map yet - add it, and go to next chunk
            if (!abilitySlots.containsKey(key)) {
                abilitySlots.put(key, new ArrayList<>());
                abilitySlots.get(key).add(chunk);
                continue;
            }

            // Key in map & list contains abilities of same type - add to list
            if (chunk.metadata().get("aspect") == abilitySlots.get(key).get(0).metadata().get("aspect")) {
                abilitySlots.get(key).add(chunk);
                continue;
            }

            // Differing type & we don't want this one - skip
            if (chunk.metadata().get("aspect") == null && aspect == AspectFlag.ASPECT
                    || chunk.metadata().get("aspect") != null && aspect == AspectFlag.BASE) {
                continue;
            }

            // Therefore, we want this one over the ones already in the map
            // so replace their list.
            abilitySlots.put(key, new ArrayList<>());
            abilitySlots.get(key).add(chunk);
        }

        // Note: we do things in the loop in this way, such that there is always
        // a "default" ability in each slot, aspects do not provide a full list
        // of abilities, only the ones they override.

        // Now we just need to add all the ability chunks into our list
        
        filteredChunks.addAll(abilitySlots.values().stream()
                .flatMap(List::stream)
                .toList());

        return filteredChunks;
    }
}
