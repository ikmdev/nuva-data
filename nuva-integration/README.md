# nuva-integration

Integration checks that open the finished data store and assert it matches the source.

**Prerequisite:** the data store written by the earlier stages.

**Skeleton behaviour:** a stub with the test dependencies and a failsafe configuration (data store path and namespace passed as system properties); it runs no tests until you add `*IT` classes under `src/test/java`. `mvn test` and `mvn compile` are unaffected.

**TODO:** `integration-01`. The full block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) is the comment in this module's `pom.xml`, and the checklist is `doc/transformation-todo.md`. The `[<id>] CONFIGURE` comments in the POM mark the lines to confirm or change (see the REFERENCE line for the sibling repositories). If this stage is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist.
