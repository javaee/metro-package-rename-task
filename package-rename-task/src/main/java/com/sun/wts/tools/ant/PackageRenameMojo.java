/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.wts.tools.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

/**
 * Creates renamed source files under the specified directory
 * (defaults to <tt>target/generated-sources/renamed-sources</tt>)
 *
 * @goal rename
 * @phase generate-sources
 * @requiresProject
 * @requiresDependencyResolution runtime
 *
 * @author Kohsuke Kawaguchi
 * @author Lukas Jungmann
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
     * @parameter expression="${renamedSourcesDir}" default-value="target/generated-sources/renamed-sources"
     */
    private File rootDir;

    /**
     * The root directory to put the renamed source files.
     *
     * @parameter expression="${srcSourcesDir}" default-value="${project.build.directory}/dependency"
     */
    private File srcDir;

    /**
     * Rename patterns as a map.
     *
     * @parameter
     */
    private Map patterns;

    /**
     * Exclude patterns when renaming (pattern to keep)
     *
     * @parameter
     */
    private String excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(patterns==null)
            throw new MojoExecutionException("No replacement patterns given");

        List<File> sources = new ArrayList<File>();
        if (srcDir != null && srcDir.exists() && srcDir.isDirectory()) {
            sources.add(srcDir);
        } else {
            for (String p: (List<String>) project.getCompileSourceRoots()) {
                sources.add(new File(p));
            }
        }
        for (File dir : sources) {
            PackageRenameTask task = new PackageRenameTask();
            task.setProject(createAntProject());
            task.setDestdir(rootDir);
            task.setSrcDir(dir);
            for (Map.Entry<String,String> e : (Collection<Entry<String,String>>)patterns.entrySet()) {
                RenamePattern pattern = new RenamePattern(e.getKey(), e.getValue());
                pattern.setExcludes(excludes);
                task.addConfiguredPattern(pattern);
            }
            task.execute();
        }
            if (project != null) {
            project.addCompileSourceRoot(rootDir.getAbsolutePath());
        }
    }

    private Project createAntProject() {
        Project p = new Project();

        DefaultLogger antLogger = new DefaultLogger();
        antLogger.setOutputPrintStream( System.out );
        antLogger.setErrorPrintStream( System.err );
        antLogger.setMessageOutputLevel( getLog().isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO );
        p.addBuildListener(antLogger);
        return p;
    }
}
