# nuva-starterdata

Creates the shared data store (`${dataStoreLocation}/${dataStore}`, by default `./datastore/nuva`) and writes the starter
data every later stage builds on.

**What it does at `install`:**

1. copies the base Tinkar starter data (`tinkar-starter-data`, versions in the parent `pom.xml`) into `./datastore/input-data`,
2. loads it into a new data store with `tinkar-maven-plugin:load-data`,
3. runs `run-nuva-starterdata` from [plugin/nuva-starterdata-maven-plugin](../plugin/nuva-starterdata-maven-plugin) to
   add the NUVA starter data on top.

The NUVA author concept (`NUVA Author`) is implemented. Re-running the module rebuilds the data store from scratch
(its `clean` execution removes the old store).

**Prerequisite:** none. It needs network access to Maven Central for the base starter data on the first build.

**TODO:** `starterdata-01` (add the remaining starter data, for example identifier-source concepts). The full block is the
comment in `NuvaStarterDataMojo.composeAdditionalStarterData`; the checklist is `doc/transformation-todo.md`. If the stage
is not needed, remove the module from the parent `pom.xml`, from `run-data-pipeline.sh`/`.bat` and from the checklist
(TODO `setup-02`).
