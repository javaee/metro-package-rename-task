/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackageRenameTask extends MatchingTask {
    private File destDir;
    private File srcDir;

    private List<RenamePattern> patterns = new ArrayList<RenamePattern>();
    private List<Command> commands = new ArrayList<Command>();

    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void addConfiguredPattern( RenamePattern p ) {
        p.addCommands(commands);
        patterns.add(p);
    }

    public void execute() throws BuildException {
        log("performing package renaming",Project.MSG_INFO);

        String[] files = getDirectoryScanner(srcDir).getIncludedFiles();
        for (String relPath : files) {
            File sfile = new File(srcDir,relPath);

            // compute the target file name
            RenamePattern match = findBestMatch(relPath);
            String dstRelPath = match != null ? match.convertPath(relPath) : relPath;

            File dfile = new File(destDir,dstRelPath);

            process(sfile,dfile);
        }
    }

    /**
     * Finds best matching pattern for provided path.
     *
     * If rel path is com.sun.istack.tools and there are patterns
     * com.sun.istack and com.sun.istack.tools, always the latter will be resolved
     *
     * @param relPath relPath to check patterns against
     * @return best matching pattern
     */
    private RenamePattern findBestMatch(String relPath) {
        RenamePattern result = null;
        for (RenamePattern pattern : patterns) {
            if (pattern.matches(relPath) && (result == null || result.getFrom().length() < pattern.getFrom().length())) {
                result = pattern;
            }
        }
        return result;
    }

    /**
     * Perform copy while performing renaming inside files.
     */
    private void process(File src, File dest) {
        if(src.lastModified() < dest.lastModified()) {
            log("skipping "+dest,Project.MSG_VERBOSE);
            return;     // no need to reprocess
        }

        log("generating "+dest,Project.MSG_VERBOSE);

        dest.getParentFile().mkdirs();

        try {
            BufferedReader in = new BufferedReader(new FileReader(src));
            BufferedWriter out = new BufferedWriter(new FileWriter(dest));

            String line;
            while((line=in.readLine())!=null) {
                out.write(process(line));
                out.newLine();
            }
            in.close();
            out.close();
        } catch( IOException e ) {
            throw new BuildException(e);
        }
    }

    /**
     * Perform renaming in one line.
     */
    private String process(String line) {
        for (Command cmd : commands) {
            line = cmd.replace(line);
        }
        return line;
    }
}
