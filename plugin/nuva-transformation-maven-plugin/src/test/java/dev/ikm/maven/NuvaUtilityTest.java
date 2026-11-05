package dev.ikm.maven;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NuvaUtilityTest {

    private static final UUID NAMESPACE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void generatedUuidIsStableAndDependsOnNamespaceAndId() {
        UUID id = NuvaUtility.generateUUID(NAMESPACE, "ABC1");
        assertEquals(id, NuvaUtility.generateUUID(NAMESPACE, "ABC1"));
        assertNotEquals(id, NuvaUtility.generateUUID(NAMESPACE, "ABC2"));
        assertNotEquals(id, NuvaUtility.generateUUID(new UUID(0, 0), "ABC1"));
    }

    @Test
    void authorConceptIsNamedAndDerivedFromTheNamespace() {
        assertEquals(NuvaUtility.AUTHOR_NAME, NuvaUtility.getUserConcept(NAMESPACE).description());
        assertEquals(NuvaUtility.generateUUID(NAMESPACE, NuvaUtility.AUTHOR_NAME),
                NuvaUtility.getUserConcept(NAMESPACE).asUuidArray()[0]);
    }
}
