package io.github.jenboc.smite_build_api.web;

import java.util.List;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;

public record AbridgedDecodedQuery(
        QueryType type,
        List<AbridgedGodMention> primaryGods,
        List<AbridgedGodMention> opponentGods,
        List<String> wantedItems,
        List<String> excludedItems,
        List<GodStatType> wantedStats,
        List<GodStatType> excludedStats,
        String rawQuery
) {
    
    public static AbridgedDecodedQuery summariseDecodedQuery(DecodedQuery query) {
        return new AbridgedDecodedQuery(
                query.type(),
                query.primaryGods().stream().map(AbridgedGodMention::summariseGodMention).toList(),
                query.opponentGods().stream().map(AbridgedGodMention::summariseGodMention).toList(),
                query.wantedItems().stream().map(Item::getName).toList(),
                query.excludedItems().stream().map(Item::getName).toList(),
                query.wantedStats(),
                query.excludedStats(),
                query.rawQuery()
        );
    }

    public record AbridgedGodMention(
            String godName,
            AspectFlag aspect
    ) {

        public static AbridgedGodMention summariseGodMention(GodMention mention) {
            return new AbridgedGodMention(mention.god().getName(), mention.aspect());
        }
    }
}
