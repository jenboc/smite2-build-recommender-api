package io.github.jenboc.smite_build_api.loading;

/**
 * A Runtime Exception which represents an error which has ocurred during Data Loading
 */
public class DataLoadException extends RuntimeException {
    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
