package io.github.jenboc.smite_build_api.indexing;

/**
 * Represents an error occuring during index file writing
 */
public class IndexWriteException extends RuntimeException {
    public IndexWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
