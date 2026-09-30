package io.github.jenboc.smite_build_api.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class DataLoaderTests {

    @Autowired
    private DataLoader dataLoader;

    @Test 
    void loadsRealData() {
        assertFalse(dataLoader.getAllGods().isEmpty());
        assertFalse(dataLoader.getAllItems().isEmpty());
        assertTrue(dataLoader.getGodByName("merlin").isPresent());
        assertTrue(dataLoader.getItemByName("gauntlet of thebes").isPresent());
    }
}
