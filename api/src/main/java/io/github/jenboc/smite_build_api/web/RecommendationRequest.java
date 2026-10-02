package io.github.jenboc.smite_build_api.web;

/**
 * DTO which represents a request for a recommendation
 * @param query the raw query
 */
public record RecommendationRequest(String query) {}
