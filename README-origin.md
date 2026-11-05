# nuva-data origin

How to obtain, place and package the licensed source data. The origin artifact is the input of the transformation
pipeline.

> **Licensed content is never committed.** `.gitignore` excludes `*.zip`. Keep the files in `~/Downloads` (or another
> directory passed with `-Dinput.directory`).

## 1. Obtain the source files

The following files are expected. **The names below are placeholders**: confirm the real names with the data owner and set
them in the parent `pom.xml`.

| Property in `pom.xml` | Placeholder file name | What it is |
|-----------------------|-----------------------|------------|
| `source.file.name` | `NUVA_${source.version}.zip` | main licensed release (`source.version` is also `REPLACE_ME`) |
| `additional.source.file.name` | `NUVA_ADDITIONAL_${source.version}.zip` | additional input, e.g. a mapping file. Delete the property and its entry in `nuva-origin/pom.xml` if there is none |

Where to download them: **REPLACE_ME** (set `origin.url` in `pom.xml` once known; the references link the publisher's
download page there).

## 2. Place the files

Put the files in `~/Downloads` (property `input.directory`). Use a different directory with
`-Dinput.directory=/path/to/dir`.

Before any packaging step runs, `nuva-origin` checks (maven-enforcer-plugin, rule `requireFilesExist`) that every
expected file exists. If not, the build fails and lists **all** missing files with their full paths.

## 3. What origin packaging must produce

Origin packaging is **not implemented** in this skeleton (TODOs `origin-01` .. `origin-04`, as comment blocks in the `generateDataLocal` profile of `nuva-origin/pom.xml`). When it is, it must produce:

* a zip `nuva-origin-<nuva-origin.version>.zip` containing `src/<selected source files>` and `META-INF/MANIFEST.MF`
  (the layout used by the reference repositories),
* attached to the `nuva-origin` module, with Maven coordinates
  `dev.ikm.data.nuva:nuva-origin:${nuva-origin.version}:zip`,
* installed to `~/.m2` with profile `generateDataLocal`, and deployed only when `-DaltDeploymentRepository=<id>::<url>` is passed.

## 4. Build commands and expected results

```
mvn clean install --projects nuva-origin --also-make -PgenerateDataLocal -Dmaven.build.cache.enabled=false
```

* Source files missing: fails in the enforcer rule `require-origin-inputs`, listing every missing file.
* Source files present, packaging not implemented: this module passes but produces no origin artifact, so
  `nuva-pipeline` then fails because Maven cannot resolve `dev.ikm.data.nuva:nuva-origin:${nuva-origin.version}:zip`.

To deploy instead of install (never the default):

```
mvn clean deploy --projects nuva-origin --also-make -DaltDeploymentRepository=<id>::REPLACE_ME -Dmaven.build.cache.enabled=false
```
