package com.sun.wts.tools.ant;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DefaultLogger;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Map.Entry;

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

    /**
     * Rename patterns as a map.
     */
    private Map patterns;


    public void execute() throws MojoExecutionException, MojoFailureException {
        for( String dir : (List<String>)project.getCompileSourceRoots() ) {
            PackageRenameTask task = new PackageRenameTask();
            task.setProject(createAntProject());
            task.setDestdir(rootDir);
            task.setSrcDir(new File(dir));
            for (Map.Entry<String,String> e : (Collection<Entry<String,String>>)patterns.entrySet()) {
                task.addConfiguredPattern(new RenamePattern(e.getKey(),e.getValue()));
            }
            task.execute();
        }
    }

    private Project createAntProject() {
        Project project = new Project();

        DefaultLogger antLogger = new DefaultLogger();
        antLogger.setOutputPrintStream( System.out );
        antLogger.setErrorPrintStream( System.err );
        antLogger.setMessageOutputLevel( getLog().isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO );
        project.addBuildListener(antLogger);
        return project;
    }
}
