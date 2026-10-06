package io.github.jenboc.smite_build_api.recommending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.retrieval.GodMentionRetriever;
import io.github.jenboc.smite_build_api.retrieval.GodStatTypeRetriever;
import io.github.jenboc.smite_build_api.retrieval.ItemRetriever;
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextGathererGeneralTests {

    @Mock
    private GodMentionRetriever godRetriever;

    @Mock
    private ItemRetriever itemRetriever;

    @Mock
    private GodStatTypeRetriever statRetriever;

    @Mock
    private VectorRetriever vecRetriever;

    private ContextGatherer contextGatherer;

    @BeforeEach
    void setUp() {
        contextGatherer = new ContextGatherer(
                godRetriever,
                itemRetriever,
                statRetriever,
                vecRetriever
        );
    }

    @Test
    void gathersGeneralQueryUsingRawQuery() {
        IndexedChunk first = chunk("first");
        IndexedChunk second = chunk("second");

        when(vecRetriever.retrieveByString(
                "How should I build this god?",
                15
        )).thenReturn(List.of(first, second));

        DecodedQuery query = new DecodedQuery(
                QueryType.GENERAL,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "How should I build this god?"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertEquals(List.of(first, second), result);

        verify(vecRetriever).retrieveByString(
                "How should I build this god?",
                15
        );
    }

    @Test
    void doesNotUseOtherRetrievers() {
        when(vecRetriever.retrieveByString("query", 15))
                .thenReturn(List.of());

        DecodedQuery query = new DecodedQuery(
                QueryType.GENERAL,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "query"
        );

        contextGatherer.gather(query);

        verifyNoInteractions(
                godRetriever,
                itemRetriever,
                statRetriever
        );
    }

    private static IndexedChunk chunk(String text) {
        return new IndexedChunk(
                text,
                Map.of("type", "ability", "name", text),
                List.of()
        );
    }
}
