package dev.ikm.maven;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the code, the POM comments and doc/transformation-todo.md in sync:
 * every DEVELOPER TODO block is complete and has a checklist entry, every UnsupportedOperationException ID has a
 * block, and every checklist entry is backed by a block or is a developer-supplied fixture.
 *
 * <p>Blocks live in Java sources (stage methods) and in {@code pom.xml} comments (modules that only need Maven
 * configuration).</p>
 */
class TodoChecklistTest {

    private static final Pattern THROWN_ID = Pattern.compile("new UnsupportedOperationException\\(\"([a-z]+-\\d\\d) not implemented\"\\)");
    private static final Pattern BLOCK_ID = Pattern.compile("DEVELOPER TODO \\[([a-z]+-\\d\\d)\\]");
    private static final Pattern CHECKLIST_ID = Pattern.compile("^- \\[[ x]\\] \\*\\*([a-z]+-\\d\\d)\\*\\*", Pattern.MULTILINE);
    private static final List<String> LABELS = List.of("WHAT:", "INPUT:", "OUTPUT:", "EXAMPLE:", "DONE WHEN:", "REFERENCE:");

    private static Path repoRoot() {
        Path p = Path.of("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("doc/transformation-todo.md"))) {
            p = p.getParent();
        }
        if (p == null) {
            throw new IllegalStateException("doc/transformation-todo.md not found above " + Path.of("").toAbsolutePath());
        }
        return p;
    }

    /** All Java sources under src/main and all pom.xml files of the repository, except build output. */
    private static String allTodoSources() throws IOException {
        StringBuilder all = new StringBuilder();
        try (Stream<Path> files = Files.walk(repoRoot())) {
            for (Path f : files.filter(TodoChecklistTest::isTodoSource).toList()) {
                all.append(Files.readString(f)).append('\n');
            }
        }
        return all.toString();
    }

    private static boolean isTodoSource(Path f) {
        String path = f.toString().replace('\\', '/');
        if (path.contains("/target/") || path.contains("/.git/")) {
            return false;
        }
        return path.endsWith("/pom.xml") || (path.contains("/src/main/") && path.endsWith(".java"));
    }

    private static Set<String> match(Pattern pattern, String text) {
        Set<String> ids = new TreeSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            ids.add(m.group(1));
        }
        return ids;
    }

    @Test
    void everyBlockIsCompleteAndListed() throws IOException {
        String sources = allTodoSources();
        String doc = Files.readString(repoRoot().resolve("doc/transformation-todo.md"));
        Set<String> blocks = match(BLOCK_ID, sources);
        assertFalse(blocks.isEmpty());
        Set<String> listed = match(CHECKLIST_ID, doc);

        for (String id : blocks) {
            assertTrue(listed.contains(id), id + " has a DEVELOPER TODO block but is missing from doc/transformation-todo.md");
            Matcher block = Pattern.compile("DEVELOPER TODO \\[" + id + "\\][\\s\\S]*?={20,}").matcher(sources);
            assertTrue(block.find(), id + " block is not closed by a line of '=' characters");
            for (String label : LABELS) {
                assertTrue(block.group().contains(label), id + " block lacks " + label);
            }
        }
    }

    @Test
    void everyThrownIdHasABlock() throws IOException {
        String sources = allTodoSources();
        Set<String> blocks = match(BLOCK_ID, sources);
        for (String id : match(THROWN_ID, sources)) {
            assertTrue(blocks.contains(id), id + " is thrown in code but has no DEVELOPER TODO block");
        }
    }

    @Test
    void everyChecklistEntryIsBackedByABlockOrIsAFixture() throws IOException {
        Set<String> blocks = match(BLOCK_ID, allTodoSources());
        Set<String> listed = match(CHECKLIST_ID, Files.readString(repoRoot().resolve("doc/transformation-todo.md")));
        for (String id : listed) {
            assertTrue(blocks.contains(id) || id.startsWith("fixture-"), id + " is listed but has no DEVELOPER TODO block");
        }
    }
}
