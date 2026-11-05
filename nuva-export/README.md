# nuva-export

Packages the finished data store into the artifacts consumers download. They are published as
`dev.ikm.tinkar.data:nuva:<version>:<classifier>` (the references use `dev.ikm.tinkar.data:<dataset>`), so downstream
pipelines depend on them under that name.

**Prerequisite:** the data store `${dataStoreLocation}/${dataStore}` written by `nuva-pipeline`.

**Publishing is opt-in.** `maven.deploy.skip` is `true` in this module, so `mvn deploy` publishes nothing unless you
pass the `deployExport` profile explicitly:

```bash
mvn deploy --projects nuva-export --also-make -PdeployExport -DaltDeploymentRepository=<id>::REPLACE_ME -Dmaven.build.cache.enabled=false
```

**Skeleton behaviour:** a stub whose `generateDataLocal` profile (`mvn install -PgenerateDataLocal`) builds a Protocol Buffers export and a data store zip and attaches them. Without the profile it builds nothing. `mvn test` and `mvn compile` are unaffected.

**TODO:** `export-01`. The full block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) is the comment in this module's `pom.xml`, and the checklist is `doc/transformation-todo.md`. The `[<id>] CONFIGURE` comments in the POM mark the lines to confirm or change (see the REFERENCE line for the sibling repositories). If this stage is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist.
