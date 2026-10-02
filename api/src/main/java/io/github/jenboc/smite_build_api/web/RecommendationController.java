package io.github.jenboc.smite_build_api.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.jenboc.smite_build_api.recommending.Recommender;

@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    private final Recommender recommender;

    public RecommendationController(Recommender recommender) {
        this.recommender = recommender;
    }

    @PostMapping
    public RecommendationResponse recommend(@RequestBody RecommendationRequest req) {
        if (req.query() == null || req.query().isBlank()) {
            throw new EmptyQueryException(req.query());
        }

        return new RecommendationResponse(
                recommender.queryForRecommendation(req.query())
        );
    }
}
