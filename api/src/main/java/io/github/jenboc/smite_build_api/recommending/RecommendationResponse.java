package io.github.jenboc.smite_build_api.recommending;

import io.github.jenboc.smite_build_api.decoding.QueryType;

/**
 * A record of an LLM response to a recommendation request
 * @param queryType the precise query type responded to by the LLM
 * @param userQuery the raw user query given to the LLM
 * @param response the raw LLM response returned
 */
public record RecommendationResponse(
        QueryType queryType,
        String userQuery,
        String response
) {}
