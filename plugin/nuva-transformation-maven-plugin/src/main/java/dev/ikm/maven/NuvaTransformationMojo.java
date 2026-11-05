package dev.ikm.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.UUID;

/**
 * Entry point invoked by {@code nuva-pipeline/pom.xml} at the {@code install} phase (same shape as
 * {@code run-rxnorm-transformation} in rxnorm-data). The transformer is instantiated directly, as in the references.
 *
 * <p>The origin zip is copied into the build directory by maven-dependency-plugin before this goal runs, so a missing
 * origin artifact fails in Maven's own dependency resolution. Every unimplemented TODO fails the build here.</p>
 */
@Mojo(name = "run-nuva-transformation", defaultPhase = LifecyclePhase.INSTALL)
public class NuvaTransformationMojo extends AbstractMojo {

    @Parameter(property = "origin.namespace", required = true)
    private String namespaceString;

    /** The origin zip resolved from the nuva-origin artifact. */
    @Parameter(required = true)
    private File originZip;

    /** Working directory for unpacked origin data and the run report. */
    @Parameter(property = "dataOutputPath", required = true)
    private File dataOutputPath;

    /** The shared data store (${dataStoreLocation}/${dataStore}) that nuva-starterdata created. */
    @Parameter(property = "datastorePath", required = true)
    private File datastorePath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        UUID namespace;
        try {
            namespace = UUID.fromString(namespaceString);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid origin.namespace `" + namespaceString + "`; expected a UUID", e);
        }

        try {
            new NuvaTransformer(namespace).run(originZip.toPath(), dataOutputPath.toPath(), datastorePath.toPath());
        } catch (UnsupportedOperationException e) {
            throw new MojoFailureException(e.getMessage() + ". See doc/transformation-todo.md.", e);
        }
    }
}
