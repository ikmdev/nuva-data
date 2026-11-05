package dev.ikm.maven;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Placeholders for tests that need developer-supplied fixtures. They show up as "skipped" in the build output until
 * the fixture exists. See src/test/resources/README.md and doc/transformation-todo.md.
 */
class DeveloperFixtureTodoTest {

    @Test
    @Disabled("fixture-01: add a tiny origin zip fixture (see src/test/resources/README.md), then test load-01 against it")
    void loadReadsFixtureOriginZip() {
    }

    @Test
    @Disabled("fixture-02: add a few synthetic source records (see src/test/resources/README.md), then test transform-01..03")
    void transformMapsFixtureRecords() {
    }

    @Test
    @Disabled("fixture-03: add the expected target output for fixture-02, then test write-01 and validate-01..03")
    void writeMatchesExpectedOutput() {
    }
}
