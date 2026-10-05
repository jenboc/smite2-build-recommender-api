package io.github.jenboc.smite_build_api.decoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.ollama.OllamaClient;

/**
 * Tests the functions of QueryDecoder which does not make use of
 * Ollama
 * i.e. how does it respond to the LLMs generation to the decode prompt?
 */
@ExtendWith(MockitoExtension.class)
class QueryDecoderTests {

    @Mock OllamaClient ollamaClient;

    private QueryDecoder queryDecoder;

    @BeforeEach
    void setUp() {
        queryDecoder = new QueryDecoder(ollamaClient);
    }


}
