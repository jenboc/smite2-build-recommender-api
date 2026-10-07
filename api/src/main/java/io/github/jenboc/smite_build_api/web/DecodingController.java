package io.github.jenboc.smite_build_api.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryDecoder;

@RestController
@RequestMapping("/decode")
public class DecodingController {
    
    private final QueryDecoder decoder;

    public DecodingController(QueryDecoder decoder) {
        this.decoder = decoder;
    }

    @PostMapping
    public DecodedQuery decode(@RequestBody RecommendationRequest req) {
        if (req.query() == null || req.query().isBlank()) {
            throw new EmptyQueryException(req.query());
        }

        return decoder.decode(req.query());
    }
}
