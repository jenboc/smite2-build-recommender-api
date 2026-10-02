package io.github.jenboc.smite_build_api.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.chunking.Chunk;
import io.github.jenboc.smite_build_api.chunking.GodChunker;
import io.github.jenboc.smite_build_api.chunking.ItemChunker;
import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;

@ExtendWith(MockitoExtension.class)
class IndexerTests {

    @Mock private DataLoader dataLoader;
    @Mock private GodChunker godChunker;
    @Mock private ItemChunker itemChunker;
    @Mock private OllamaClient ollamaClient;

    private Indexer indexer;

    @BeforeEach
    void setUp() {
        indexer = new Indexer(dataLoader, godChunker, itemChunker, ollamaClient);
    }

    @Test
    void producesOneIndexedChunkPerChunk() {
        God merlin = new God();
        Chunk overview = new Chunk("Merlin overview", Map.of("type", "god"));
        Chunk ability = new Chunk("Merlin ability", Map.of("type", "ability"));

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of(overview, ability));
        when(ollamaClient.embed(List.of("Merlin overview", "Merlin ability")))
            .thenReturn(List.of(List.of(0.1, 0.2), List.of(0.3, 0.4)));

        IndexFile index = indexer.buildIndexFile("patch-1");

        assertEquals(2, index.chunks().size());
    }

    @Test
    void preservesChunkText() {
        God merlin = new God();
        Chunk overview = new Chunk("Merlin overview", Map.of("type", "god"));
        Chunk ability = new Chunk("Merlin ability", Map.of("type", "ability"));

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of(overview, ability));
        when(ollamaClient.embed(List.of("Merlin overview", "Merlin ability")))
            .thenReturn(List.of(List.of(0.1, 0.2), List.of(0.3, 0.4)));

        IndexFile index = indexer.buildIndexFile("patch-1");

        assertEquals("Merlin overview", index.chunks().get(0).text());
        assertEquals("Merlin ability", index.chunks().get(1).text());
    }

    @Test
    void preservesChunkMetadata() {
        God merlin = new God();
        Chunk overview = new Chunk("Merlin overview", Map.of("type", "god"));
        Chunk ability = new Chunk("Merlin ability", Map.of("type", "ability"));

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of(overview, ability));
        when(ollamaClient.embed(List.of("Merlin overview", "Merlin ability")))
            .thenReturn(List.of(List.of(0.1, 0.2), List.of(0.3, 0.4)));

        IndexFile index = indexer.buildIndexFile("patch-1");

        assertEquals(Map.of("type", "god"),
            index.chunks().get(0).metadata());
        assertEquals(Map.of("type", "ability"),
            index.chunks().get(1).metadata());
    }

    @Test
    void recordsSuppliedSourcePatch() {
        God merlin = new God();

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of());

        String expected = "Interesting Patch Name";
        IndexFile index = indexer.buildIndexFile(expected);

        assertEquals(expected, index.sourcePatch());
    }

    @Test
    void callsEmbedPerGodNotPerChunk() {
        // A god produces many chunks. For efficiency, we do not want to send
        // a POST request per individual chunk. Ollama supports batching
        // and we want to make use of that.

        // We intend that embed is called for the god's entire set of chunks,
        // i.e. using the list returned directly from GodChunker.
        
        God merlin = new God();
        
        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of(
            new Chunk("a", Map.of()),
            new Chunk("b", Map.of()),
            new Chunk("c", Map.of()),
            new Chunk("d", Map.of()),
            new Chunk("e", Map.of())
        ));
        when(ollamaClient.embed(any())).thenReturn(List.of(
            List.of(1.0),
            List.of(1.0),
            List.of(1.0),
            List.of(1.0),
            List.of(1.0)
        ));

        indexer.buildIndexFile("patch");

        // If our requirement is true, then embed will be called using the
        // text of the chunks.
        verify(ollamaClient).embed(List.of("a", "b", "c", "d", "e"));
    }

    @Test
    void doesNotCallEmbedForObjectWithNoChunks() {
        // Technically not necessary, but again is more efficient to avoid
        // sending a POST request when we don't want something embedded.

        God merlin = new God();

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of());

        indexer.buildIndexFile("patch");

        // If our requirement holds, then ollamaClient.embed won't be called
        // at all, for any arguments
        verify(ollamaClient, org.mockito.Mockito.never()).embed(any());
    }

    @Test
    void combinesGodsAndItemsIntoSameIndex() {
        God merlin = new God();
        Item thebes = new Item();

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of(thebes));
        when(godChunker.chunk(merlin)).thenReturn(List.of(new Chunk("Merlin text", Map.of("type", "god"))));
        when(itemChunker.chunk(thebes)).thenReturn(List.of(new Chunk("Thebes text", Map.of("type", "item"))));
        when(ollamaClient.embed(List.of("Merlin text"))).thenReturn(List.of(List.of(1.0)));
        when(ollamaClient.embed(List.of("Thebes text"))).thenReturn(List.of(List.of(2.0)));

        IndexFile index = indexer.buildIndexFile("patch");

        assertEquals(2, index.chunks().size());

        IndexedChunk first = index.chunks().get(0);
        IndexedChunk second = index.chunks().get(1);
        assertTrue(
            (
                first.text() == "Merlin text" && first.metadata().get("type") == "god"
                && second.text() == "Thebes text" && second.metadata().get("type") == "item"
            )
            || (
                first.text() == "Thebes text" && first.metadata().get("type") == "item"
                && second.text() == "Merlin text" && second.metadata().get("type") == "god"
            )
        );
    }

    @Test
    void throwsOnVectorMismatch() {
        // Vector mismatch, i.e. index 2 chunks but recieve != 2 vectors from ollama
        
        God merlin = new God();

        when(dataLoader.getAllGods()).thenReturn(List.of(merlin));
        when(dataLoader.getAllItems()).thenReturn(List.of());
        when(godChunker.chunk(merlin)).thenReturn(List.of(
            new Chunk("a", Map.of()), new Chunk("b", Map.of())
        ));

        // To avoid
        // (a) assuming order of operations in buildIndexFile
        // (b) getting a Mockito error for "unused stubs"
        dataLoader.getAllItems();

        // Call with 2 => throws when 1 returned
        when(ollamaClient.embed(any())).thenReturn(
            List.of(List.of(1.0))
        );
        assertThrows(IllegalStateException.class, () -> indexer.buildIndexFile("patch"));

        // Call with 2 => throws when 3 returned
        when(ollamaClient.embed(any())).thenReturn(
            List.of(List.of(1.0), List.of(1.0), List.of(1.0))
        );
        assertThrows(IllegalStateException.class, () -> indexer.buildIndexFile("patch"));
    }
}
