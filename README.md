# NUVA Data Pipeline (skeleton)

This repository is a **skeleton**. It has the build structure of `snomed-ct-data`, `rxnorm-data` and `loinc-data`
(<https://github.com/ikmdev>), but the origin packaging and the transformation are deliberately left for a developer to write.
**It is designed to fail** until the licensed source files are supplied and the TODOs are implemented. A green build
never means data was produced unless every TODO in [doc/transformation-todo.md](doc/transformation-todo.md) is done.

**Prerequisites**

* JDK 24+
* Maven 3.9.9+ (a Maven wrapper, `./mvnw`, is included and uses 3.9.11)
* Nexus Repository (optional)
* The licensed source files, see [README-origin.md](README-origin.md)

**Clone Project and Configure Maven Settings**

1. Clone the repository

   ```
   git clone https://github.com/ikmdev/nuva-data.git
   ```

2. Create a Maven settings file (`~/.m2/settings.xml`) that gives your machine access to our Nexus repositories, based on the
   [provided sample](https://ikmdev.atlassian.net/wiki/spaces/IKDT/pages/1036648449/Centralized+Documentation+for+Maven+Settings+File+Configuration)
   (placeholder: this is the page the reference repositories link to; replace it with the page that applies to your team).
   Keep your Nexus credentials in that file only; they never belong in this repository.

   > The `pom.xml` files declare no repository URLs. Without the settings file, artifacts resolve from Maven Central only,
   > so anything published to our Nexus (for example a previously deployed `nuva-origin` zip) cannot be found.

3. Change local directory to `nuva-data`

## Stage order

Stages run in the order of the `<modules>` in `pom.xml` (the same order as `rxnorm-data`):

| # | Stage | Module | Status in the skeleton |
|---|-------|--------|------------------------|
| 0 | plugin | `plugin/nuva-transformation-maven-plugin`, `plugin/nuva-starterdata-maven-plugin` | the transformation code (`NuvaTransformer`: TODO `load-01`, `transform-01..03`, `validate-01..03`, `write-01`, `report-01`; `NuvaUtility` for helpers) and the starter-data Mojo (author concept done, TODO `starterdata-01`). Both depend on tinkar-core and tinkar-composer |
| 1 | origin | `nuva-origin` | checks the source files exist (enforcer); packaging = TODO `origin-01..04` in the POM |
| 2 | starterdata | `nuva-starterdata` | loads the base Tinkar starter data into `./datastore/nuva`, then adds the NUVA author; further starter data = TODO `starterdata-01` |
| 3 | pipeline | `nuva-pipeline` | copies the origin artifact (`dependency:copy`), then runs the transformation |
| 4 | owl-transform | `nuva-owl-transform` | empty placeholder POM, TODO `owl-01` (optional) |
| 5 | reasoner | `nuva-reasoner` | empty placeholder POM, TODO `reasoner-01` (optional) |
| 6 | integration | `nuva-integration` | empty placeholder POM, TODO `integration-01` |
| 7 | export | `nuva-export` | empty placeholder POM, TODO `export-01`; publishes nothing unless `-PdeployExport` |

`./run-data-pipeline.sh` (or `run-data-pipeline.bat`) runs the stages in order, prints each stage name, stops at the first
failure, and supports `--skip <stage>` and `--dry-run`. Run `./run-data-pipeline.sh --help` for all options.

## Build and deploy commands

Origin packaging (see [README-origin.md](README-origin.md) for the input files):

```
# install the origin artifact into the local ~/.m2 repository
mvn clean install --projects nuva-origin --also-make -PgenerateDataLocal -Dmaven.build.cache.enabled=false

# deploy the origin artifact to a shared repository (id from your settings.xml; replace REPLACE_ME with the URL)
mvn clean deploy --projects nuva-origin --also-make -DaltDeploymentRepository=<id>::REPLACE_ME -Dmaven.build.cache.enabled=false
```

Transformation pipeline (after the origin artifact is in `~/.m2` or a configured repository):

```
mvn clean install -U -Dmaven.build.cache.enabled=false
```

Publish the transformed data (explicit opt-in; without `deployExport` nothing is deployed):

```
mvn deploy --projects nuva-export --also-make -PdeployExport -DaltDeploymentRepository=<id>::REPLACE_ME -Dmaven.build.cache.enabled=false
```

No real repository URL, credential or `settings.xml` content is stored in this repository.

## Expected failures on a fresh clone

| Command | Result | Why |
|---------|--------|-----|
| `mvn clean compile` | passes | no source data needed |
| `mvn test` | passes (3 skipped) | unit tests use temp directories; the 3 skipped tests wait for developer fixtures |
| `mvn clean install` | **fails in `nuva-origin`**, enforcer rule `require-origin-inputs` | the source files are not in `~/Downloads`. The message lists **every** missing file with its full path. |
| the same, with the source files present (`-Dinput.directory=<dir>`) | **fails in `nuva-pipeline`**, `dependency:copy` | `nuva-starterdata` succeeds first and creates `./datastore`; origin packaging is not implemented, so no origin artifact exists and Maven reports `Could not find artifact dev.ikm.data.nuva:nuva-origin:zip:<version>` |
| the same, once the origin artifact is installed or deployed | **fails in `nuva-pipeline`**, goal `run-nuva-transformation` | `load-01 not implemented`, then each next TODO ID as you implement the stages |
| GitHub Actions `build.yml` | red | same as `mvn clean install`; see the comment at the top of the workflow |

The build fails when the origin artifact cannot be resolved (Maven's own dependency resolution) or when a transformation
stage is unimplemented (it throws `UnsupportedOperationException` with its TODO ID, which the Mojo turns into a build
failure). The optional stage modules (`nuva-starterdata`, `nuva-owl-transform`, `nuva-reasoner`, `nuva-integration`,
`nuva-export`) are empty placeholders that build without error; their TODOs are tracked in the checklist only.

## Developer Quick Start

1. Read [doc/transformation-todo.md](doc/transformation-todo.md): it lists every TODO ID (`origin-01`, `load-01`,
   `transform-01`, ...) with the file and method where it lives. Each one has a comment block in the code or POM with
   WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE.
2. Replace the `REPLACE_ME` placeholders in the parent `pom.xml` and put the source files in place
   ([README-origin.md](README-origin.md)).
3. Run `mvn clean install` (or `./run-data-pipeline.sh`). The build stops at the next unfinished step
   (a missing origin artifact, or a TODO ID); implement that one, then repeat.
4. Add the test fixtures described in
   `plugin/nuva-transformation-maven-plugin/src/test/resources/README.md`.
5. Drop optional modules you do not need (parent `<modules>`, scripts, checklist).

Where the code lives: [plugin/nuva-transformation-maven-plugin](plugin/nuva-transformation-maven-plugin). As in the
reference repositories, the transformation code is a Maven plugin in `plugin/`, and the `nuva-pipeline` module only
configures and invokes it. Origin packaging is plain Maven configuration in `nuva-origin/pom.xml`.

Tinkar: both plugins already depend on `tinkar-core` and `tinkar-composer` (versions in the parent `pom.xml`, the same as
`rxnorm-data` and `loinc-data`), so no dependency management is needed to start writing transformation logic.

## Repository layout

```
pom.xml                                parent POM: properties, enforcer rules (JDK 24+, Maven 3.9.9+)
plugin/nuva-transformation-maven-plugin  NuvaTransformer (pipeline stages), NuvaUtility, the Mojo that runs them, unit tests
plugin/nuva-starterdata-maven-plugin   NuvaStarterDataMojo (NUVA author + TODO for more starter data)
nuva-origin/                           pom packaging; enforcer input check; profile generateDataLocal (packaging TODOs)
nuva-starterdata/ nuva-pipeline/ nuva-owl-transform/ nuva-reasoner/ nuva-integration/ nuva-export/
datastore/                             shared data store written by the stages (git-ignored)
run-data-pipeline.sh / .bat            stage orchestration
doc/                                   how-to-contribute.md, transformation-todo.md
```

## Issues and Contributions

Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/nuva-data/issues).
Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md).
