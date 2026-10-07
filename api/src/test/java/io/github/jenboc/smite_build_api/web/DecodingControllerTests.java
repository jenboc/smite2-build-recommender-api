package io.github.jenboc.smite_build_api.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryDecoder;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(DecodingController.class)
class DecodingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private QueryDecoder decoder;

    @Test
    void returnsDecodedQuery() throws Exception {
        DecodedQuery expected = new DecodedQuery(
                QueryType.BUILD_RECOMMENDATION,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "Build me a tanky Thor"
        );

        when(decoder.decode("Build me a tanky Thor"))
            .thenReturn(expected);

        MvcResult res = mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "query": "Build me a tanky Thor" }
                """))
            .andExpect(status().isOk())
            .andReturn();

        DecodedQuery actual = jsonMapper.readValue(
                res.getResponse().getContentAsString(),
                DecodedQuery.class
        );

        assertEquals(expected, actual);
    }

    @Test
    void throwsOnMalformedRequest() throws Exception {
        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void throwsOnEmptyQuery() throws Exception {
        mockMvc.perform(post("/decode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "query": "" }
                """))
            .andExpect(status().isBadRequest());
    }

}
