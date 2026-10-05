package io.github.jenboc.smite_build_api.decoding;

public enum QueryType {
    // Asking for a build recommendation
    BUILD_RECOMMENDATION,

    // Asking about item synergy
    ITEM_SYNERGY,

    // A general query, or a query that cannot be fit into either box
    GENERAL
}
