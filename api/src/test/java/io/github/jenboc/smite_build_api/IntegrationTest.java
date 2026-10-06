package io.github.jenboc.smite_build_api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = {
    "data.path=${SMITE_DATA_PATH:../data}",
    "index.path=${INDEX_PATH:../index}"
})
public @interface IntegrationTest {
}
