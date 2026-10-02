package io.github.jenboc.smite_build_api.web;

public class EmptyQueryException extends RuntimeException {
    
    public EmptyQueryException(String query) {
        super("Query cannot be null or empty, received: '" + query + "'");
    }
}
