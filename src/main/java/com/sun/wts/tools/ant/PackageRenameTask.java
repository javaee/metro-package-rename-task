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
