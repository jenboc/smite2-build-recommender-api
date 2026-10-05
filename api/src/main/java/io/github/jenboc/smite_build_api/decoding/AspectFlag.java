package io.github.jenboc.smite_build_api.decoding;

/**
 * Flag signalling whether an aspect is being used or not,
 * or if we don't know either way
 */
public enum AspectFlag {
    // No Aspect
    BASE,

    // Yes Aspect
    ASPECT,

    // We don't know
    UNSPECIFIED
}
