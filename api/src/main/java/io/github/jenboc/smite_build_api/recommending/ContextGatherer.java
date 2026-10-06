package io.github.jenboc.smite_build_api.recommending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.retrieval.GodMentionRetriever;
import io.github.jenboc.smite_build_api.retrieval.GodStatTypeRetriever;
import io.github.jenboc.smite_build_api.retrieval.ItemRetriever;
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;

/**
 * Combines retrievers in order to gather all chunks required to answer a query
 */
@Component
public class ContextGatherer {

    private final GodMentionRetriever godRetriever;
    private final ItemRetriever itemRetriever;
    private final GodStatTypeRetriever statRetriever;
    private final VectorRetriever vecRetriever;

    public ContextGatherer(
            GodMentionRetriever godRetriever,
            ItemRetriever itemRetriever,
            GodStatTypeRetriever statRetriever,
            VectorRetriever vectorRetriever
    ) {
        this.godRetriever = godRetriever;
        this.itemRetriever = itemRetriever;
        this.statRetriever = statRetriever;
        this.vecRetriever = vectorRetriever;
    }

    /**
     * Gather the required context to answer a query
     * @param query the decoded query
     * @returns the context the LLM requires to answer the query
     */
    public List<IndexedChunk> gather(DecodedQuery query) {
        return switch (query.type()) {
            case QueryType.BUILD_RECOMMENDATION -> recommendationGather(query);
            case QueryType.ITEM_SYNERGY -> synergyGather(query);
            default -> generalGather(query);
        };
    }

    private List<IndexedChunk> recommendationGather(DecodedQuery query) {
        Set<GodStatType> targetStats = gatherStatsToRetrieveFor(query);
        
        Set<GodMention> mentionedGods = new HashSet<>();
        mentionedGods.addAll(query.primaryGods());
        mentionedGods.addAll(query.opponentGods());

        List<IndexedChunk> ctx = new ArrayList<>();

        // Get chunks for mentioned gods
        ctx.addAll(mentionedGods.stream()
                .flatMap(m -> godRetriever.retrieve(m).stream())
                .toList());

        // Get chunks for inferred + mentioned stats
        ctx.addAll(targetStats.stream()
                .flatMap(s -> statRetriever.retrieve(s).stream())
                .toList());

        // Get chunks for mentioned items
        ctx.addAll(query.wantedItems().stream()
                .flatMap(i -> itemRetriever.retrieve(i).stream())
                .toList());

        // Gather generally based upon the query
        ctx.addAll(generalGather(query));

        return removeItemsAndDedupe(ctx, query.excludedItems());
    }

    private List<IndexedChunk> synergyGather(DecodedQuery query) {
        // Vector search using the chunks of the "wanted items"
        List<IndexedChunk> ctx = new ArrayList<>();

        List<IndexedChunk> wantedItems = query.wantedItems().stream()
            .flatMap(i -> itemRetriever.retrieve(i).stream())
            .toList();

        // Add wanted items
        ctx.addAll(wantedItems);

        // Add related items
        ctx.addAll(wantedItems.stream()
                .flatMap(c -> vecRetriever.retrieve(c.embedding(), 5).stream())
                .toList());

        return removeItemsAndDedupe(ctx, query.excludedItems());
    }

    private List<IndexedChunk> generalGather(DecodedQuery query) {
        return vecRetriever.retrieveByString(query.rawQuery(), 15);
    }

    private List<IndexedChunk> removeItemsAndDedupe(List<IndexedChunk> ctx, List<Item> toRemove) {
        Set<String> toRemoveNames = toRemove.stream()
            .map(i -> i.getName().toLowerCase())
            .collect(Collectors.toSet());

        return new ArrayList<>(new HashSet<>(ctx.stream()
            .filter(c -> !"item".equals(c.metadata().get("type"))
                    || !toRemoveNames.contains(c.metadata().get("name").toLowerCase()))
            .toList()));
    }

    private Set<GodStatType> gatherStatsToRetrieveFor(DecodedQuery query) {
        Set<GodStatType> gatheredStats = new HashSet<>();

        for (GodMention mention : query.primaryGods()) {
            // Add scaling
            gatheredStats.addAll(StatAnalyser.analyseScalingStats(mention));

            // If character is intended to be tanky, fetch protection
            if (mention.god().getRoles().contains(God.Role.SOLO)
                    || mention.god().getRoles().contains(God.Role.SUPPORT)) {
                
                gatheredStats.addAll(Set.of(GodStatType.MAGICAL_PROTECTION, GodStatType.PHYSICAL_PROTECTION));
            }
        }

        // We also want to look for stats the user explicitly mentioned
        gatheredStats.addAll(query.wantedStats());
        
        // We want to ignore stats the user rejected
        gatheredStats.removeAll(query.excludedStats());

        return gatheredStats;
    }
}
