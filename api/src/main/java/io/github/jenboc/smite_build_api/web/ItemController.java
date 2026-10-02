package io.github.jenboc.smite_build_api.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.Item;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final DataLoader dataLoader;

    public ItemController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return dataLoader.getAllItems();
    }

    @GetMapping("/{name}")
    public Item getItemByName(@PathVariable String name) {
        return dataLoader.getItemByName(name)
            .orElseThrow(() -> new ItemNotFoundException(name));
    }
}
