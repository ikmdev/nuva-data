package dev.ikm.maven;

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.UUID;

/**
 * Helper functions shared by the transformation stages (same role as {@code SnomedUtility} and {@code RxnormUtility}
 * in the reference repositories). Add NUVA-specific helpers here as the stages need them: code-to-concept lookups,
 * date parsing, identifier handling, and so on.
 */
public class NuvaUtility {

    /**
     * Name of the author concept written by nuva-starterdata-maven-plugin (NuvaStarterDataMojo.AUTHOR_NAME).
     * Keep the two identical: the concept is found again by its name-derived UUID.
     */
    public static final String AUTHOR_NAME = "NUVA Author";

    private NuvaUtility() {
    }

    /** Deterministic UUID for a source identifier within the pipeline namespace. */
    public static UUID generateUUID(UUID namespace, String id) {
        return UuidT5Generator.get(namespace, id);
    }

    /** The author of all transformed data: the concept created by the starter-data stage. */
    public static Concept getUserConcept(UUID namespace) {
        return Concept.make(AUTHOR_NAME, generateUUID(namespace, AUTHOR_NAME));
    }

    /** The path the transformed data is written on (the same path the reference pipelines use). */
    public static Concept getPathConcept() {
        return TinkarTerm.DEVELOPMENT_PATH;
    }
}
