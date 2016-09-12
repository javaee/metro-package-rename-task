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

    private boolean excludeNonRenamed;

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

    public void setExcludeNonRenamed(boolean excludeNonRenamed) {
        this.excludeNonRenamed = excludeNonRenamed;
    }

    public void execute() throws BuildException {
        log("performing package renaming",Project.MSG_INFO);

        String[] files = getDirectoryScanner(srcDir).getIncludedFiles();
        for (String relPath : files) {
            File sfile = new File(srcDir,relPath);

            // compute the target file name
            String dstRelPath=null;
            for (RenamePattern p : patterns) {
                dstRelPath = p.convertPath(relPath);
                if(dstRelPath!=null)
                    break;
            }
            if(dstRelPath==null) {
                // didn't match any name
                if (excludeNonRenamed) {
                    continue;
                }
                dstRelPath = relPath;
            }
            File dfile = new File(destDir,dstRelPath);

            process(sfile,dfile);
        }
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
