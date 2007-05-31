package com.sun.wts.tools.ant;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Creates renamed source files under the specified directory
 * (defaults to <tt>target/renamed-sources</tt>)
 *
 * @goal rename
 * @phase compile
 * @requiresProject
 * @requiresDependencyResolution runtime
 *
 * @author Kohsuke Kawaguchi
 */
public class PackageRenameMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The root directory to put the renamed source files.
     *
     * @parameter expression="${renamedSourcesDir}" default-value="target/renamed-sources"
     */
    private File rootDir;


    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
