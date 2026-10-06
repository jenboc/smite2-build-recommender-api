package io.github.jenboc.smite_build_api.decoding;

import java.util.List;

import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;

public record DecodedQuery(
        QueryType type,
        List<GodMention> primaryGods,
        List<GodMention> opponentGods,
        List<Item> wantedItems,
        List<Item> excludedItems,
        List<GodStatType> wantedStats,
        List<GodStatType> excludedStats,
        String rawQuery
) {}
