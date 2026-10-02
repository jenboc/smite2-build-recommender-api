package io.github.jenboc.smite_build_api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.God;

@WebMvcTest(GodController.class)
class GodControllerTests {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataLoader dataLoader;

    private God god(String name) {
        God god = new God();
        god.setName(name);
        return god;
    }

    @Test
    void getAllGodsReturnsEveryGod() throws Exception {
        when(dataLoader.getAllGods()).thenReturn(List.of(god("Merlin"), god("Artio")));

        mockMvc.perform(get("/gods"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Merlin"))
            .andExpect(jsonPath("$[1].name").value("Artio"));
    }

    @Test
    void getGodByNameReturnsMatchingGod() throws Exception {
        when(dataLoader.getGodByName("merlin")).thenReturn(Optional.of(god("Merlin")));

        mockMvc.perform(get("/gods/merlin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Merlin"));
    }

    @Test
    void getGodByNameReturns404WhenNotFound() throws Exception {
        when(dataLoader.getGodByName("fake")).thenReturn(Optional.empty());

        mockMvc.perform(get("/gods/fake"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No god found with name: fake"));
    }
}
