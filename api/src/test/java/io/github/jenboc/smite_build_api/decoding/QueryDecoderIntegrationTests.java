package io.github.jenboc.smite_build_api.decoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jenboc.smite_build_api.IntegrationTest;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;

/**
 * Tests QueryDecoder while actually making use of Ollama
 * i.e. does decodeStringQuery() return the results we expect?
 */
@IntegrationTest 
class QueryDecoderIntegrationTests {

    @Autowired
    private QueryDecoder queryDecoder;

    @ParameterizedTest(name = "\"{0}\"")
    @MethodSource("decodingCases")
    void decodesAsExpected(String query, ExpectedQuery expected) {
        assertMatches(expected, queryDecoder.decode(query));
    }

    // Cases:
    static Stream<Arguments> decodingCases() {
        return Stream.of(
            Arguments.of(
                "Give me a build for Nu Wa",
                ExpectedQuery.recommendationFor("Nu Wa", AspectFlag.UNSPECIFIED)
            ),
            Arguments.of(
                "Give me a build for Pele with her aspect",
                ExpectedQuery.recommendationFor("Pele", AspectFlag.ASPECT)
            ),
            Arguments.of(
                "Build for Thor, no aspect",
                ExpectedQuery.recommendationFor("Thor", AspectFlag.BASE)
            ),
            Arguments.of(
                "Give me a build for Nu Wa against an aspected Janus, which uses the Rod of Tahuti",
                new ExpectedQuery(
                    QueryType.BUILD_RECOMMENDATION,
                    List.of(new ExpectedGodMention("Nu Wa", AspectFlag.UNSPECIFIED)),
                    List.of(new ExpectedGodMention("Janus", AspectFlag.ASPECT)),
                    List.of("Rod of Tahuti"),
                    List.of(), List.of(), List.of()
                )
            ),
            Arguments.of(
                "What items work well with Rod of Tahuti",
                new ExpectedQuery(
                    QueryType.ITEM_SYNERGY,
                    List.of(), List.of(),
                    List.of("Rod of Tahuti"),
                    List.of(), List.of(), List.of()
                )
            )
        );
    }

    private void assertMatches(ExpectedQuery expected, DecodedQuery actual) {
        assertEquals(expected.type(), actual.type());
        
        // God Fields Match
        assertThat(toExpectedGodMentions(actual.primaryGods()))
            .containsExactlyInAnyOrderElementsOf(expected.primaryGods());
        assertThat(toExpectedGodMentions(actual.opponentGods()))
            .containsExactlyInAnyOrderElementsOf(expected.opponentGods());

        // Item Fields Match
        assertThat(toItemNames(actual.wantedItems()))
            .containsExactlyInAnyOrderElementsOf(expected.wantedItems());
        assertThat(toItemNames(actual.excludedItems()))
            .containsExactlyInAnyOrderElementsOf(expected.excludedItems());

        // Stat Fields Match
        assertThat(actual.wantedStats())
            .containsExactlyInAnyOrderElementsOf(expected.wantedStats());
        assertThat(actual.excludedStats())
            .containsExactlyInAnyOrderElementsOf(expected.excludedStats());
    }

    private List<ExpectedGodMention> toExpectedGodMentions(List<GodMention> mentions) {
        return mentions.stream()
            .map(m -> new ExpectedGodMention(m.god().getName(), m.aspect()))
            .toList();
    }

    private List<String> toItemNames(List<Item> items) {
        return items.stream()
            .map(Item::getName)
            .toList();
    }

    // DTOs which use String god names instead of God models
    record ExpectedGodMention(String godName, AspectFlag aspect) {}
    record ExpectedQuery(
            QueryType type,
            List<ExpectedGodMention> primaryGods,
            List<ExpectedGodMention> opponentGods,
            List<String> wantedItems,
            List<String> excludedItems,
            List<GodStatType> wantedStats,
            List<GodStatType> excludedStats
    ) {

        // convenience constructor for the common case: just a primary god, nothing else
        static ExpectedQuery recommendationFor(String godName, AspectFlag aspect) {
            return new ExpectedQuery(
                QueryType.BUILD_RECOMMENDATION,
                List.of(new ExpectedGodMention(godName, aspect)),
                List.of(), List.of(), List.of(), List.of(), List.of()
            );
        }
    }
}
