package io.github.jenboc.smite_build_api.ollama;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("integration")
@SpringBootTest
class OllamaClientIntegrationTests {
    
    @Autowired
    private OllamaClient client;

    @Test 
    void embedReturnsVectors() {
        List<List<Double>> result = client.embed(List.of("hello world"));
        
        // Should return a single vector since we sent a single string
        assertTrue(result.size() == 1);
        System.out.println("'hello world' vector length: " + result.get(0).size());
    }

    @Test
    void generateReturnsText() {
        String resp = client.generate("Say hello in one word");
        assertFalse(resp.isBlank());
        System.out.println("Response: " + resp);
    }
}
