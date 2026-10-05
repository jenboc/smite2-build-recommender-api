package io.github.jenboc.smite_build_api.decoding;

import io.github.jenboc.smite_build_api.model.God;

/**
 * Represents a mention of a god in the query
 * @param god the god that was mentioned
 * @param aspect whether or not the aspect is being used
 */
public record GodMention(
        God god,
        AspectFlag aspect
) {}
