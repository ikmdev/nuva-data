# Test fixtures

Unit tests in this module must run **without** licensed origin data. Put only small, synthetic, non-licensed samples
here. Never commit content copied from a licensed release; invent the codes and labels.

Each fixture below is a developer TODO (listed in `doc/transformation-todo.md`). Until it exists, the matching test in
`DeveloperFixtureTodoTest` is `@Disabled` and shows as skipped.

| ID | Fixture to add | Suggested location | Used by |
|----|----------------|--------------------|---------|
| fixture-01 | A tiny origin zip with the same layout `origin-03` produces (`src/<files>` and `META-INF/MANIFEST.MF`) | `src/test/resources/fixtures/origin-sample.zip` | load-01 |
| fixture-02 | A handful of synthetic source records: a few concepts, descriptions and relationships, including one deliberately broken record per validation rule | `src/test/resources/fixtures/source/` | transform-01..03, validate-01..02 |
| fixture-03 | The expected target output for fixture-02 (counts and a few concrete identifiers) | `src/test/resources/fixtures/expected/` | write-01, validate-03, report-01 |

Keep each fixture small enough to read in an editor.
