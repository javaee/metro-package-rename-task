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

package org.kohsuke.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class PackageRenameTask extends Task {
    private File destDir;
    private final Set sources = new HashSet();

    private String from;
    private String to;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    public void addConfiguredSource(FileSet fs) {
        sources.add(fs);
    }

    public void execute() throws BuildException {
        destDir.mkdirs();

        Set sourceRelatives = new HashSet();

        // we'll check existing files in the destdir and remove
        // files that are not present in the source folder.
        // since we have fewer files right now than after the processing is over,
        // obtain the file list right now.
        FileSet d = new FileSet();
        d.setDir(destDir);
        String[] dests = d.getDirectoryScanner(project).getIncludedFiles();

        // process files
        for (Iterator itr = sources.iterator(); itr.hasNext();) {
            FileSet fs = (FileSet) itr.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                String file = files[i];
                process( new File(ds.getBasedir(),file), new File(destDir,file) );
                sourceRelatives.add(file);
            }
        }

        // remove files that aren't in the source folder
        for (int i = 0; i < dests.length; i++) {
            String dest = dests[i];
            if(!sourceRelatives.contains(dest)) {
                File destFile = new File(destDir,dest);
                log("deleting "+destFile,Project.MSG_VERBOSE);
                destFile.delete();
            }
        }
    }

    private void process(File src, File dest) {
        if(src.lastModified() < dest.lastModified()) {
            log("skipping "+dest,Project.MSG_VERBOSE);
            return;     // no need to reprocess
        }

        log("processing "+dest,Project.MSG_VERBOSE);

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

    private String process(String line) {

    }
}
