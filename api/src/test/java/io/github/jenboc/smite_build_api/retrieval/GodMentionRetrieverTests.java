package io.github.jenboc.smite_build_api.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.model.God;

@ExtendWith(MockitoExtension.class)
class GodMentionRetrieverTests {

    @Mock private IndexContainer indexContainer;

    private GodMentionRetriever retriever;

    @BeforeEach
    void setUp() {
        retriever = new GodMentionRetriever(indexContainer);
    }

    private God god(String godName) {
        God god = new God();
        god.setName(godName);
        return god;
    }

    private GodMention godMention(String godName, AspectFlag aspect) {
        return new GodMention(god(godName), aspect);
    }

    private IndexedChunk godIndexedChunk(String godName) {
        return new IndexedChunk("God Chunk", Map.of("type", "god", "name", godName), List.of(1.0));
    }

    private IndexedChunk abilityIndexedChunk(String godName, String slot, String aspect) {
        Map<String, String> metadata = new HashMap<>(Map.of("type", "ability", "god", godName));
        metadata.put("aspect", aspect);

        return new IndexedChunk("Ability Chunk",
                metadata,
                List.of(1.0));
    }

    private IndexedChunk aspectIndexedChunk(String godName) {
        return new IndexedChunk("Aspect Chunk",
                Map.of("type", "aspect", "god", godName),
                List.of(1.0));
    }

    private void mockContainerWith(List<IndexedChunk> chunks) {
        when(indexContainer.getChunks())
            .thenReturn(chunks);
    }

    private void assertEqualsUpToOrdering(List<IndexedChunk> a, List<IndexedChunk> b) {
        assertThat(a)
            .containsExactlyInAnyOrderElementsOf(b);
    }

    @Test
    void returnsEmptyListWhenIndexIsEmpty() {
        mockContainerWith(List.of());
        
        List<IndexedChunk> retrieved = retriever.retrieve(godMention("merlin", AspectFlag.UNSPECIFIED));
        assertEquals(List.of(), retrieved);
    }

    @Test
    void returnsAllGodChunksIfUnspecified() {
        String godName = "merlin";
        List<IndexedChunk> chunks = List.of(
                godIndexedChunk(godName),
                abilityIndexedChunk(godName, "passive", null),
                abilityIndexedChunk(godName, "passive", "aspect"),
                aspectIndexedChunk(godName)
        );

        mockContainerWith(chunks);

        List<IndexedChunk> retrieved = retriever.retrieve(godMention(godName, AspectFlag.UNSPECIFIED));
        assertEquals(chunks, retrieved);
    }

    @Test
    void returnsBaseChunksIfBase() {
        String godName = "merlin";
        List<IndexedChunk> chunks = List.of(
                godIndexedChunk(godName),
                abilityIndexedChunk(godName, "passive", null),
                abilityIndexedChunk(godName, "passive", "aspect"),
                aspectIndexedChunk(godName)
        );

        mockContainerWith(chunks);

        List<IndexedChunk> retrieved = retriever.retrieve(godMention(godName, AspectFlag.BASE));
        assertEqualsUpToOrdering(
                List.of(chunks.get(0), chunks.get(1)),
                retrieved
        );
    }

    @Test
    void returnsOverridenChunksIfAspect() {
        String godName = "merlin";
        List<IndexedChunk> chunks = List.of(
                godIndexedChunk(godName),
                abilityIndexedChunk(godName, "passive", null),
                abilityIndexedChunk(godName, "passive", "aspect"),
                aspectIndexedChunk(godName)
        );

        mockContainerWith(chunks);

        List<IndexedChunk> retrieved = retriever.retrieve(godMention(godName, AspectFlag.ASPECT));
        // Always contains overview
        assertEqualsUpToOrdering(
                List.of(chunks.get(0), chunks.get(2), chunks.get(3)),
                retrieved
        );
    }
    
    @Test
    void onlyReturnsGodAbilityAndAspectChunks() {
        String godName = "merlin";
        List<IndexedChunk> chunks = List.of(
                godIndexedChunk(godName),
                new IndexedChunk("Item", Map.of("type", "item"), List.of(1.0))
        );

        mockContainerWith(chunks);

        List<IndexedChunk> retrieved = retriever.retrieve(godMention(godName, AspectFlag.UNSPECIFIED));
        assertEquals(List.of(chunks.get(0)), retrieved);
    }
}
