package io.github.jenboc.smite_build_api.web;

/**
 * Represents a response to a recommendation request
 * @param recommendation the raw string response to the request
 */
public record RecommendationResponse(String recommendation) {}
