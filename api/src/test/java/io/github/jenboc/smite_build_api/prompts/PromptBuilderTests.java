package io.github.jenboc.smite_build_api.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

class PromptBuilderTests {

    private PromptBuilder builder;

    @BeforeEach 
    void setUp() {
        builder = new PromptBuilder();
    }

    private static List<IndexedChunk> context() {
        return List.of(
                new IndexedChunk("chunk 1", Map.of("type", "yippee"), List.of(1.0)),
                new IndexedChunk("chunk 2", Map.of("type", "test"), List.of(2.0))
        );
    }

    private static List<String> examples() {
        return List.of(
                "This is example 1.",
                "This is example 2, I think."
        );
    }

    @Test
    void unconfiguredBuilderBuildsEmptyString() {
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void correctlyAddsInstructions() {
        String prompt = builder.withInstructions("do a backflip").build();

        assertEquals(
                """
                ==== INSTRUCTIONS ====
                do a backflip
                """.strip(),
                prompt
        );
    }

    @Test
    void correctlyAddsContext() {
        String prompt = builder.withContext(context()).build();

        assertEquals(
                """
                ==== CONTEXT ====
                [YIPPEE] chunk 1

                [TEST] chunk 2
                """.strip(),
                prompt
        );
    }

    @Test
    void correctlyAddsExamples() {
        String prompt = builder.withExamples(examples()).build();

        assertEquals(
                """
                ==== EXAMPLES ====
                Example 1:
                This is example 1.

                Example 2:
                This is example 2, I think.
                """.strip(),
                prompt
        );
    }

    @Test
    void correctlyAddsUserQuery() {
        String prompt = builder.withUserQuery("Build me a house").build();

        assertEquals(
                """
                ==== USER QUERY ====
                Build me a house
                """.strip(),
                prompt
        );
    }

    @Test
    void correctlyAddsDomainKnowledge() {
        String prompt = builder.withDomainKnowledge("A square is a type of 4 sided polygon").build();

        assertEquals(
                """
                ==== DOMAIN KNOWLEDGE ====
                A square is a type of 4 sided polygon
                """.strip(),
                prompt
        );
    }

    @Test
    void constructionPerservesCallOrder() {
        String prompt = builder
            .withDomainKnowledge("A square is a type of 4 sided polygon")
            .withUserQuery("What 4 sided polygons do you know?")
            .withInstructions("Respond to the USER QUERY only using DOMAIN KNOWLEDGE")
            .build();

        assertEquals(
                """
                ==== DOMAIN KNOWLEDGE ====
                A square is a type of 4 sided polygon

                ==== USER QUERY ====
                What 4 sided polygons do you know?

                ==== INSTRUCTIONS ====
                Respond to the USER QUERY only using DOMAIN KNOWLEDGE
                """.strip(),
                prompt
        );
    }

    @ParameterizedTest
    @MethodSource("returnsSameTestData")
    void methodsReturnSelf(Function<PromptBuilder, PromptBuilder> builderMutation) {
        PromptBuilder returned = builderMutation.apply(builder);
        assertEquals(builder.build(), returned.build());
    }

    @ParameterizedTest
    @MethodSource("callTwiceTestData")
    void callingTwiceOverwrites(
            Consumer<PromptBuilder> builderMutation,
            String expectedBuildResult
    ) {
        builderMutation.accept(builder);
        assertEquals(expectedBuildResult, builder.build());
    }

    static Stream<Arguments> returnsSameTestData() {
        return Stream.of(
                Arguments.of((Function<PromptBuilder, PromptBuilder>)b -> b.withContext(context())),
                Arguments.of((Function<PromptBuilder, PromptBuilder>)b -> b.withInstructions("test")),
                Arguments.of((Function<PromptBuilder, PromptBuilder>)b -> b.withDomainKnowledge("test")),
                Arguments.of((Function<PromptBuilder, PromptBuilder>)b -> b.withExamples(examples())),
                Arguments.of((Function<PromptBuilder, PromptBuilder>)b -> b.withUserQuery("test"))
        );
    }

    static Stream<Arguments> callTwiceTestData() {
        return Stream.of(
                Arguments.of(
                    (Consumer<PromptBuilder>)b -> b.withContext(context()).withContext(List.of()),
                    "==== CONTEXT ===="
                ),
                Arguments.of(
                    (Consumer<PromptBuilder>)b -> b.withInstructions("test").withInstructions(""),
                    "==== INSTRUCTIONS ===="
                ),
                Arguments.of(
                    (Consumer<PromptBuilder>)b -> b.withDomainKnowledge("test").withDomainKnowledge(""),
                    "==== DOMAIN KNOWLEDGE ===="
                ),
                Arguments.of(
                    (Consumer<PromptBuilder>)b -> b.withExamples(examples()).withExamples(List.of()),
                    "==== EXAMPLES ===="
                ),
                Arguments.of(
                    (Consumer<PromptBuilder>)b -> b.withUserQuery("query").withUserQuery(""),
                    "==== USER QUERY ===="
                )
        );
    }
}
