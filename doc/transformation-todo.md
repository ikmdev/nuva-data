# Developer TODO checklist

Every ID below has a `DEVELOPER TODO [id]` comment block (WHAT / INPUT / OUTPUT / EXAMPLE / DONE WHEN / REFERENCE) at
the place listed here: in `NuvaTransformer` for the transformation stages (each throws
`UnsupportedOperationException("<id> not implemented")`), in `NuvaStarterDataMojo` for the starter data, and in a
`pom.xml` for project setup, origin packaging and the optional stage modules. `TodoChecklistTest` fails the build if the blocks and this file drift apart, so when you add or
remove a TODO, edit both.

Tick a box when its **DONE WHEN** condition (in the code block) holds. Work top to bottom: later steps use earlier output.

Java paths are relative to `plugin/<plugin>/src/main/java/dev/ikm/maven/`.
`REFERENCE` lines in the code point into the reference repositories (`snomed-ct-data`, `rxnorm-data`, `loinc-data`,
`snomed-ct-loinc-data`, all under https://github.com/ikmdev).

## 0. Project setup

Obtain the licensed source files and place them in `~/Downloads` (see `README-origin.md`). Then, in the parent `pom.xml`:

- [ ] **setup-01** — Replace the placeholder properties (`source.version`, file names, `packager.name`, `origin.url`, `origin.namespace`)
- [ ] **setup-02** — Keep or drop the optional modules; update the run scripts and this checklist to match
- [ ] **setup-03** — Confirm the Tinkar versions and the base data store against the sibling pipelines

## 1. Origin packaging (`nuva-origin`)

The check that the source files exist works (enforcer rule in `nuva-origin/pom.xml`). The `generateDataLocal` profile of
that POM is a working stub modeled on the reference origin modules: it unzips the source files, writes the manifest and
packages the origin zip. Each TODO block sits next to the plugin configuration it concerns, and `[origin-NN] CONFIGURE`
comments mark the individual lines to change.

- [ ] **origin-01** — Unzip the source archives: confirm one execution per source file and the folder names (`process-origin`, `process-additional-origin`)
- [ ] **origin-02** — Select the required release files: narrow the `src` fileSet includes and excludes (`assemble-origin-artifact`)
- [ ] **origin-03** — Confirm the manifest and artifact layout (`create-origin-manifest`, `assemble-origin-artifact`)
- [ ] **origin-04** — Verify local install and deploy of the origin artifact (no POM change expected)

## 2. Starter data (`nuva-starterdata`)

`nuva-starterdata` loads the base Tinkar starter data into a new data store (`${dataStoreLocation}/${dataStore}`) and
runs `nuva-starterdata-maven-plugin` on top of it. The NUVA author concept is already implemented in
`NuvaStarterDataMojo`; the rest is yours.

- [ ] **starterdata-01** — Add the remaining starter data (`NuvaStarterDataMojo.composeAdditionalStarterData`)

## 3. Transformation pipeline (`nuva-pipeline`)

`nuva-pipeline` copies the origin artifact with `dependency:copy`; until `origin-01..04` produce it, Maven fails there with
`Could not find artifact`. These steps are in `NuvaTransformer`, run in this order. `NuvaUtility` (same package) is the
place for shared helpers, and `tinkar-core` / `tinkar-composer` are already on the plugin's classpath.

- [ ] **load-01** — Read the origin artifact (`load`)
- [ ] **transform-01** — Map source concepts to the target model (`transformConcepts`)
- [ ] **transform-02** — Map source descriptions (`transformDescriptions`)
- [ ] **transform-03** — Map source relationships (`transformRelationships`)
- [ ] **validate-01** — No orphan references (`checkNoOrphanReferences`)
- [ ] **validate-02** — Required fields present (`checkRequiredFields`)
- [ ] **validate-03** — Counts within expected ranges (`checkCountsInRange`)
- [ ] **write-01** — Write transformed output (`write`)
- [ ] **report-01** — Write the run report (`report`)

## 4. Optional stage modules

Working stubs that run the standard Tinkar goals against the data store, modeled on `rxnorm-data`. Each TODO block is a
comment in the module's `pom.xml`, next to the configuration to confirm; `[<id>] CONFIGURE` comments mark the lines where
there is a setting to change.

- [ ] **owl-01** — Confirm or drop the OWL transformation (`nuva-owl-transform`)
- [ ] **reasoner-01** — Choose the reasoner (`nuva-reasoner`; property `reasoner-type` in the parent `pom.xml`)
- [ ] **integration-01** — Add integration tests (`nuva-integration`; failsafe is configured, the `*IT` classes are yours)
- [ ] **export-01** — Confirm the export artifacts and their coordinates (`nuva-export`, published as `dev.ikm.tinkar.data:nuva`; profile `generateDataLocal`; never publishes by default)

## 5. Developer-supplied test fixtures

Described in `plugin/nuva-transformation-maven-plugin/src/test/resources/README.md`. The matching tests in
`DeveloperFixtureTodoTest` are `@Disabled` until the fixture exists; remove the `@Disabled` and implement the test.

- [ ] **fixture-01** — Tiny origin zip with the layout `origin-03` produces (used by load-01)
- [ ] **fixture-02** — A few synthetic source records, incl. one broken record per validation rule (transform-01..03, validate-01..02)
- [ ] **fixture-03** — Expected target output for fixture-02 (write-01, validate-03, report-01)

## 6. Known open questions (not TODOs, decide early)

- Whether the author name `NUVA Author` and its definition text are what the data owner wants. The name lives in two
  places that must stay identical: `NuvaStarterDataMojo.AUTHOR_NAME` and `NuvaUtility.AUTHOR_NAME`.
- Whether NUVA depends on another terminology's artifacts (the references use an `<upstream>.version` property
  and `maven-dependency-plugin:unpack` in the starterdata module).
- Which Maven repository holds the origin artifact for other developers (set up your `settings.xml`, see
  `README.md`); `nuva-pipeline` resolves it like any other dependency.
- The data store lives in `./datastore` (property `dataStoreLocation`), not in `./target` as in the references, so that
  building one stage with `--also-make clean` cannot delete the store the earlier stages built.
