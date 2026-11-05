package dev.ikm.maven;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Every stage fails with its TODO ID until the developer implements it. Delete a test when its stage is done. */
class NuvaTransformerTest {

    private final NuvaTransformer transformer = new NuvaTransformer(new UUID(0, 0));
    private final Path path = Path.of("unused");

    private static void assertTodo(String id, Runnable stage) {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, stage::run);
        assertEquals(id + " not implemented", e.getMessage());
    }

    @Test
    void stagesFailWithTheirTodoId() {
        assertTodo("load-01", () -> transformer.load(path, path));
        assertTodo("transform-01", () -> transformer.transformConcepts(path));
        assertTodo("transform-02", () -> transformer.transformDescriptions(path));
        assertTodo("transform-03", () -> transformer.transformRelationships(path));
        assertTodo("validate-01", transformer::checkNoOrphanReferences);
        assertTodo("validate-02", transformer::checkRequiredFields);
        assertTodo("validate-03", transformer::checkCountsInRange);
        assertTodo("write-01", () -> transformer.write(path));
        assertTodo("report-01", () -> transformer.report(path));
    }

    @Test
    void runStopsAtTheFirstUnimplementedStage() {
        assertTodo("load-01", () -> transformer.run(path, path, path));
    }
}
