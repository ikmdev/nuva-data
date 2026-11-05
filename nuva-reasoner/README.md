# nuva-reasoner

Optional: classifies the data store so inferred relationships are stored.

**Prerequisite:** the data store written by `nuva-pipeline` (and `nuva-owl-transform` if kept).

**Skeleton behaviour:** a stub that runs `tinkar-maven-plugin:run-full-reasoner` at `install`, using the reasoner named by `reasoner-type` in the parent `pom.xml`. `mvn test` and `mvn compile` are unaffected.

**TODO:** `reasoner-01`. The full block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) is the comment in this module's `pom.xml`, and the checklist is `doc/transformation-todo.md`. The `[<id>] CONFIGURE` comments in the POM mark the lines to confirm or change (see the REFERENCE line for the sibling repositories). If this stage is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist.
