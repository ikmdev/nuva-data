# nuva-export

Packages the finished data store into the artifacts consumers download.

**Prerequisite:** the data store `${dataStoreLocation}/${dataStore}` written by `nuva-pipeline`.

**Publishing is opt-in.** `maven.deploy.skip` is `true` in this module, so `mvn deploy` publishes nothing unless you
pass the `deployExport` profile explicitly:

```bash
mvn deploy --projects nuva-export --also-make -PdeployExport -DaltDeploymentRepository=<id>::REPLACE_ME -Dmaven.build.cache.enabled=false
```

**Skeleton behaviour:** an empty placeholder module; it builds without doing anything. `mvn test` and `mvn compile` are unaffected.

**TODO:** `export-01`. The full block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) is the comment in this module's `pom.xml`, and the checklist is `doc/transformation-todo.md`. Replace the comment with the real plugin configuration (see the REFERENCE line). If this stage is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist.
