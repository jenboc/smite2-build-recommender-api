package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;

/**
 * Class which facilitates similarity searches between chunks
 */
public class SimilaritySearch {

    /**
     * Check the cosine similarity of two vectors.
     *
     * @param u the first vector
     * @param v the second vector
     * @returns cos(theta), where theta is the angle between them
     */
    public static double cosineSimilarity(List<Double> u, List<Double> v) {
        if (u.size() != v.size()) {
            throw new IllegalArgumentException(
                "Can only check similarity of vectors of same dimension, was given "
                + "dimensions: " + u.size() + ", " + v.size()
            );
        }

        double dot = 0;
        double normSquaredU = 0;
        double normSquaredV = 0;

        for (int i = 0; i < u.size(); i++) {
            dot += u.get(i) * v.get(i);
            normSquaredU += u.get(i) * u.get(i);
            normSquaredV += v.get(i) * v.get(i);
        }

        if (normSquaredU == 0 || normSquaredV == 0) {
            throw new IllegalArgumentException(
                "Can only check similarity of non-zero vectors"
            );
        }

        return dot / Math.sqrt(normSquaredU * normSquaredV);
    }
}
