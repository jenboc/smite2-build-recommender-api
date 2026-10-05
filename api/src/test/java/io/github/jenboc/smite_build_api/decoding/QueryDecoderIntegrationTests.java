package io.github.jenboc.smite_build_api.decoding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests QueryDecoder while actually making use of Ollama
 * i.e. does decodeStringQuery() return the results we expect?
 */
@Tag("integration")
@SpringBootTest
class QueryDecoderIntegrationTests {

    @Autowired
    private QueryDecoder queryDecoder;

    @ParameterizedTest
    @MethodSource("testCases")
    void decodesAsExpected(String query, DecodedQuery expected) {
        assertEquals(expected, queryDecoder.decode(query));
    }

    static Stream<Arguments> testCases() {
        return Stream.of(
            Arguments.of("placeholder", new DecodedQuery())
        );
    }
}
