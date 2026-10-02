package io.github.jenboc.smite_build_api.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.jenboc.smite_build_api.recommending.Recommender;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Recommender recommender;

    @Test
    void returnsRecommendation() throws Exception {
        when(recommender.queryForRecommendation("Build me a tanky Thor"))
            .thenReturn("Build Thor with...");

        mockMvc.perform(post("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "query": "Build me a tanky Thor" }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendation")
                    .value("Build Thor with..."));
    }

    @Test
    void throwsOnMalformedRequest() throws Exception {
        mockMvc.perform(post("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void throwsOnEmptyQuery() throws Exception {
        mockMvc.perform(post("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "query": "" }
                """))
            .andExpect(status().isBadRequest());
    }
}
