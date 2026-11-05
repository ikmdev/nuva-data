package dev.ikm.maven;

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.template.Definition;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Identifier;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.UUID;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;

/**
 * Writes the NUVA starter data into the data store at {@code datastorePath}, on top of the base Tinkar starter data
 * that {@code nuva-starterdata/pom.xml} loads first (same shape as {@code run-snomed-starterdata} in snomed-ct-data).
 *
 * <p>Implemented: the NUVA author concept. Everything else the pipeline needs in the store before the transformation
 * runs is TODO starterdata-01.</p>
 */
@Mojo(name = "run-nuva-starterdata", defaultPhase = LifecyclePhase.INSTALL)
public class NuvaStarterDataMojo extends AbstractMojo {

    /**
     * Name of the author concept. Keep it identical to NuvaUtility.AUTHOR_NAME in nuva-transformation-maven-plugin:
     * the transformation refers to this concept by name-derived UUID, and the two plugins share no code.
     */
    static final String AUTHOR_NAME = "NUVA Author";

    @Parameter(property = "origin.namespace", required = true)
    String namespaceString;

    @Parameter(property = "datastorePath", required = true)
    private String datastorePath;

    @Parameter(property = "controllerName", defaultValue = "Open SpinedArrayStore")
    private String controllerName;

    @Override
    public void execute() throws MojoExecutionException {
        UUID namespace;
        try {
            namespace = UUID.fromString(namespaceString);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid origin.namespace `" + namespaceString + "`; expected a UUID", e);
        }

        try {
            CachingService.clearAll();
            ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, new File(datastorePath));
            PrimitiveData.selectControllerByName(controllerName);
            PrimitiveData.start();

            Composer composer = new Composer("Nuva Starter Data Composer");
            EntityProxy.Concept author = EntityProxy.Concept.make(AUTHOR_NAME, UuidT5Generator.get(namespace, AUTHOR_NAME));
            Session session = composer.open(State.ACTIVE, author, TinkarTerm.PRIMORDIAL_MODULE, TinkarTerm.PRIMORDIAL_PATH);

            composeAuthor(session, author);
            composeAdditionalStarterData(session, namespace);

            composer.commitSession(session);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write NUVA starter data to " + datastorePath, e);
        } finally {
            PrimitiveData.stop();
        }
    }

    private static void composeAuthor(Session session, EntityProxy.Concept author) {
        session.compose((ConceptAssembler concept) -> concept
                .concept(author)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text(AUTHOR_NAME)
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text(AUTHOR_NAME)
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                // Placeholder wording: confirm the publisher's name with the data owner.
                .attach((Definition definition) -> definition
                        .language(ENGLISH_LANGUAGE)
                        .text("Author of the NUVA starter data and of the data transformed from the NUVA source")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(author.asUuidArray()[0].toString()))
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(TinkarTerm.USER)));
    }

    private static void composeAdditionalStarterData(Session session, UUID namespace) {
        // ============================================================================
        // DEVELOPER TODO [starterdata-01]: Add the remaining starter data
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // WHAT: Compose every concept the transformation needs to find already in the data store,
        //       beyond the author above: typically the identifier-source concepts for the
        //       source's own identifiers (one per identifier scheme), source-specific modules,
        //       and any description-type or status concepts that Tinkar does not already define.
        //       Delete this block when the list is complete.
        // INPUT: The source documentation (which identifier schemes, modules and code lists exist)
        //        and `namespace`; derive each concept's UUID with
        //        UuidT5Generator.get(namespace, <stable source key>).
        // OUTPUT: Additional concepts committed in the same session, each with a fully qualified
        //         name, a synonym, a definition, an identifier and a stated axiom.
        // EXAMPLE: identifier scheme "NUVA Identifier" (made-up) ->
        //          EntityProxy.Concept.make("NUVA Identifier", UuidT5Generator.get(namespace, "NUVA Identifier")),
        //          composed like the author, with .isA(TinkarTerm.IDENTIFIER_SOURCE).
        // DONE WHEN: After `mvn install` of nuva-starterdata, every concept that transform-01..03 refers
        //            to by proxy exists in the data store, and running the goal twice on a fresh store
        //            gives identical UUIDs.
        // REFERENCE: snomed-ct-data/plugin/snomed-ct-starterdata-maven-plugin/src/main/java/dev/ikm/maven/
        //            SnomedStarterDataMojo.java (identifier-source and module concepts) and
        //            loinc-data/plugin/loinc-starterdata-maven-plugin/src/main/java/dev/ikm/maven/
        //            LoincStarterDataMojo.java.
        // ============================================================================
    }
}
