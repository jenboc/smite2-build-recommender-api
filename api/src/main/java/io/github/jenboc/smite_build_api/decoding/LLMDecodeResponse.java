package io.github.jenboc.smite_build_api.decoding;

import java.util.List;

import io.github.jenboc.smite_build_api.model.GodStatType;

/**
 * Represents the content of the LLM's response to a decode request. Any
 * response that cannot be deserialised into this format is incorrect.
 * @param primaryGods names of gods that the user wishes to play as
 * @param opponentGods names of gods the user is up against
 * @param primaryAspects aspect flags for the primary gods (in order)
 * @param opponentAspects aspect flags for the opponent gods (in order
 * @param wantedItems names of items the user wishes to use
 * @param excludedItems names of items the user wishes to avoid
 * @param wantedStats the names of stats the user wishes to use
 * @param excludedStats the names of stats the user wishes to avoid
 */
public record LLMDecodeResponse(
        QueryType type,
        List<String> primaryGods,
        List<String> opponentGods,
        List<AspectFlag> primaryAspects,
        List<AspectFlag> opponentAspects,
        List<String> wantedItems,
        List<String> excludedItems,
        List<GodStatType> wantedStats,
        List<GodStatType> excludedStats
) {}
