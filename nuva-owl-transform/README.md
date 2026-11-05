# nuva-owl-transform

Optional: converts stated relationships/axioms in the data store into the OWL form the reasoner needs.

**Prerequisite:** the data store `${dataStoreLocation}/${dataStore}` written by `nuva-pipeline`.

**Skeleton behaviour:** a stub that runs `tinkar-maven-plugin:run-owl-transformer` on the data store at `install`. `mvn test` and `mvn compile` are unaffected.

**TODO:** `owl-01`. The full block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) is the comment in this module's `pom.xml`, and the checklist is `doc/transformation-todo.md`. The `[<id>] CONFIGURE` comments in the POM mark the lines to confirm or change (see the REFERENCE line for the sibling repositories). If this stage is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist.
