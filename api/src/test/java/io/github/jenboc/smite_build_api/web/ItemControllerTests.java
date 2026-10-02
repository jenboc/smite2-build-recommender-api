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
import io.github.jenboc.smite_build_api.model.Item;

@WebMvcTest(ItemController.class)
class ItemControllerTests {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataLoader dataLoader;

    private Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    @Test
    void getAllItemsReturnsEveryItem() throws Exception {
        when(dataLoader.getAllItems()).thenReturn(List.of(item("Gauntlet of Thebes"), item("Aegis of Acceleration")));

        mockMvc.perform(get("/items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Gauntlet of Thebes"))
            .andExpect(jsonPath("$[1].name").value("Aegis of Acceleration"));
    }

    @Test
    void getItemByNameReturnsMatchingItem() throws Exception {
        when(dataLoader.getItemByName("gauntlet of thebes")).thenReturn(Optional.of(item("Gauntlet of Thebes")));

        mockMvc.perform(get("/items/{name}", "gauntlet of thebes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Gauntlet of Thebes"));
    }

    @Test
    void getGodByNameReturns404WhenNotFound() throws Exception {
        when(dataLoader.getItemByName("fake")).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/fake"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No item found with name: fake"));
    }
}
