package io.github.jenboc.smite_build_api.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.God;

@RestController
@RequestMapping("/gods")
public class GodController {

    private final DataLoader dataLoader;

    public GodController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @GetMapping
    public List<God> getAllGods() {
        return dataLoader.getAllGods();
    }

    @GetMapping("/{name}")
    public God getGodByName(@PathVariable String name) {
        return dataLoader.getGodByName(name)
            .orElseThrow(() -> new GodNotFoundException(name));
    }
}
