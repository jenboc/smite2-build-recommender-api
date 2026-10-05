package io.github.jenboc.smite_build_api.decoding;

/**
 * Represents a runtime exception caused by an illegal/malformed
 * LLMDecodeResponse
 * @see LLMDecodeResponse
 */
public class IllegalLLMDecodeResponse extends RuntimeException {
    
    public IllegalLLMDecodeResponse(String message) {
        super(message);
    }

    public IllegalLLMDecodeResponse(String message, Throwable cause) {
        super(message, cause);
    }
}
