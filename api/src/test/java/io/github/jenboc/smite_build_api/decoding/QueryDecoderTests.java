package io.github.jenboc.smite_build_api.decoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.config.JacksonConfig;
import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests the functions of QueryDecoder which does not make use of
 * Ollama
 * i.e. how does it respond to the LLMs generation to the decode prompt?
 */
@ExtendWith(MockitoExtension.class)
class QueryDecoderTests {

    @Mock OllamaClient ollamaClient;
    @Mock DataLoader dataLoader;

    private final JsonMapper jsonMapper = buildConfiguredMapper();

    private QueryDecoder queryDecoder;

    @BeforeEach
    void setUp() {
        queryDecoder = new QueryDecoder(dataLoader, ollamaClient, jsonMapper);
    }

    private static JsonMapper buildConfiguredMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JacksonConfig().jacksonCustomiser().customize(builder);
        return builder.build();
    }

    private void mockEmptyGetAll() {
        when(dataLoader.getAllGods())
            .thenReturn(List.of());

        when(dataLoader.getAllItems())
            .thenReturn(List.of());
    }

    private List<God> mockGetGodByName(List<String> names) {
        List<God> gods = new ArrayList<>();

        for (String n : names) {
            God god = new God();
            god.setName(n);

            when(dataLoader.getGodByName(n))
                .thenReturn(Optional.of(god));

            gods.add(god);
        }

        return gods;
    }
    
    private List<Item> mockGetItemByName(List<String> names) {
        List<Item> items = new ArrayList<>();

        for (String n : names) {
            Item item = new Item();
            item.setName(n);

            when(dataLoader.getItemByName(n))
                .thenReturn(Optional.of(item));

            items.add(item);
        }

        return items;
    }

    @Test
    void throwsIfCannotDeserialiseResponse() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn("{ Invalid JSON ]");

        assertThrows(IllegalLLMDecodeResponse.class, 
                () -> queryDecoder.decode("query"));
    }

    @Test
    void throwsIfPrimaryGodsAndAspectsAreDifferentSizes() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "BUILD_RECOMMENDATION",
                    "primary_gods": ["god1"],
                    "opponent_gods": [],
                    "primary_aspects": [],
                    "opponent_aspects": [],
                    "wanted_items": [],
                    "excluded_items": [],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        assertThrows(IllegalLLMDecodeResponse.class,
                () -> queryDecoder.decode("query"));
    }

    @Test
    void throwsIfOpponentGodsandAspectsAreDifferentSizes() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "BUILD_RECOMMENDATION",
                    "primary_gods": [],
                    "opponent_gods": ["god1"],
                    "primary_aspects": [],
                    "opponent_aspects": [],
                    "wanted_items": [],
                    "excluded_items": [],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        assertThrows(IllegalLLMDecodeResponse.class,
                () -> queryDecoder.decode("query"));
    }

    @Test
    void decodedQueryStoresRaw() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "ITEM_SYNERGY",
                    "primary_gods": [],
                    "opponent_gods": [],
                    "primary_aspects": [],
                    "opponent_aspects": [],
                    "wanted_items": [],
                    "excluded_items": [],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        DecodedQuery query = queryDecoder.decode("boop boop beep boop boop");
        assertEquals(
                "boop boop beep boop boop",
                query.rawQuery()
        );
    }

    @Test
    void correctlyRetrievesMentionedItems() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "BUILD_RECOMMENDATION",
                    "primary_gods": [],
                    "opponent_gods": [],
                    "primary_aspects": [],
                    "opponent_aspects": [],
                    "wanted_items": ["item1"],
                    "excluded_items": ["item2"],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        List<Item> items = mockGetItemByName(List.of("item1", "item2"));
        DecodedQuery decoded = queryDecoder.decode("query");

        assertEquals(List.of(items.get(0)), decoded.wantedItems());
        assertEquals(List.of(items.get(1)), decoded.excludedItems());
    }

    @Test
    void correctlyCombinesPrimaryGodsAndAspects() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "BUILD_RECOMMENDATION",
                    "primary_gods": ["god1", "god2"],
                    "opponent_gods": [],
                    "primary_aspects": ["ASPECT", "BASE"],
                    "opponent_aspects": [],
                    "wanted_items": [],
                    "excluded_items": [],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        mockGetGodByName(List.of("god1", "god2"));

        DecodedQuery decoded = queryDecoder.decode("query");

        // Assert using a loop as to not assume an ordering.
        for (GodMention mention : decoded.primaryGods()) {
            if (mention.god().getName() == "god1") {
                assertEquals(AspectFlag.ASPECT, mention.aspect());
            } else {
                assertEquals(AspectFlag.BASE, mention.aspect());
            }
        }
    }

    @Test
    void correctlyCombinesOpponentGodsAndAspects() {
        mockEmptyGetAll();

        when(ollamaClient.generate(any(), any()))
            .thenReturn(
                """
                {
                    "type": "BUILD_RECOMMENDATION",
                    "primary_gods": [],
                    "opponent_gods": ["god1", "god2"],
                    "primary_aspects": [],
                    "opponent_aspects": ["ASPECT", "BASE"],
                    "wanted_items": [],
                    "excluded_items": [],
                    "wanted_stats": [],
                    "excluded_stats": []
                }
                """.strip()
            );

        mockGetGodByName(List.of("god1", "god2"));

        DecodedQuery decoded = queryDecoder.decode("query");

        // Assert using a loop as to not assume an ordering.
        for (GodMention mention : decoded.opponentGods()) {
            if (mention.god().getName() == "god1") {
                assertEquals(AspectFlag.ASPECT, mention.aspect());
            } else {
                assertEquals(AspectFlag.BASE, mention.aspect());
            }
        }
    }
}
