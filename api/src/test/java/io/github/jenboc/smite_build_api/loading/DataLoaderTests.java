package io.github.jenboc.smite_build_api.loading;

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
    void loadsTestData() {
        assertFalse(dataLoader.getAllGods().isEmpty());
        assertFalse(dataLoader.getAllItems().isEmpty());
        assertTrue(dataLoader.getGodByName("merlin").isPresent());
        assertTrue(dataLoader.getItemByName("gauntlet of thebes").isPresent());
        assertTrue(dataLoader.getItemByName("aegis of acceleration").isPresent());
    }

    @Test
    void getGodByNameIsCaseInsensitive() {
        assertTrue(dataLoader.getGodByName("mErLiN").isPresent());
    }

    @Test
    void getItemByNameIsCaseInsensitive() {
        assertTrue(dataLoader.getItemByName("gAuNTLET of thEBES").isPresent());
    }
}
