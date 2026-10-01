package io.github.jenboc.smite_build_api.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class SimilaritySearchTests {

    private final SimilaritySearch search = new SimilaritySearch();

    @Test
    void consineReturnsCorrectForUnitVectors() {
        // Unit vectors simply calculate the dot-product

        // Dim = 1
        List<Double> u = List.of(1.0);
        List<Double> v = List.of(1.0);
        assertEquals(1, search.cosineSimilarity(u, v));
        assertEquals(1, search.cosineSimilarity(v, u));

        // Dim = 3
        double x = 1.0 / Math.sqrt(2.0);
        u = List.of(x, 0.0, x);
        v = List.of(0.0, x, x);
        assertEquals(0.5, search.cosineSimilarity(u, v));
        assertEquals(0.5, search.cosineSimilarity(v, u));
    }

    @Test
    void cosineReturnsCorrectForNonUnitVectors() {
        // Non-unit vectors should divide by product of norms

        // Dim = 1
        List<Double> u = List.of(100.0);
        List<Double> v = List.of(9293.0);
        assertEquals(1.0, search.cosineSimilarity(u, v));
        assertEquals(1.0, search.cosineSimilarity(v, u));

        // Dim = 3
        u = List.of(143.0, 0.0, 143.0);
        v = List.of(0.0, 527.0, 527.0);
        assertEquals(0.5, search.cosineSimilarity(u, v));
        assertEquals(0.5, search.cosineSimilarity(v, u));
    }

    @Test 
    void cosineThrowsIllegalArgumentWhenAVectorIsZero() {
        List<Double> u = List.of(0.0, 0.0, 0.0);
        List<Double> v = List.of(1.0, 1.0, 1.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> search.cosineSimilarity(u, v)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> search.cosineSimilarity(v, u)
        );
    }

    @Test
    void cosineThrowsIllegalArgumentWhenVectorsHaveDifferentDimension() {
        List<Double> u = List.of(1.0);
        List<Double> v = List.of(2.0, 2.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> search.cosineSimilarity(u, v)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> search.cosineSimilarity(v, u)
        );
    }
}
