package com.sun.wts.tools.ant;

import java.io.File;
import java.util.regex.Pattern;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * One rename command.
 *
 * @author Kohsuke Kawaguchi
 */
public class RenamePattern {
    /**
     * Package name to rename from.
     * e.g., "org.acme.foo."
     */
    private String from;
    /**
     * Package name to rename to.
     * e.g., "org.acme.internal.foo."
     */
    private String to;

    /**
     * Package name that match these and the {@link #from} will be skipped.
     */
    private final List<String> excludes = new ArrayList<String>();


    public RenamePattern() {
    }

    public RenamePattern(String from, String to) {
        this.from = from;
        this.to = to;
    }

    String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from+'.';
    }

    String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to+'.';
    }

    public void setExcludes(String excludes) {
        StringTokenizer st = new StringTokenizer(excludes, ",");
        while(st.hasMoreTokens())
             this.excludes.add(st.nextToken().trim()+".");
    }

    /**
     * Adds all the rename commands to the given list.
     */
    public void addCommands( List<Command> commands ) {
        // dot-delimited pattern "org.acme.foo."
        commands.add(new Command(
            Pattern.compile(from,Pattern.LITERAL),
            to));
        // dot-delimited pattern with ';' termination. "org.acme.foo;"
        // used for package statement.
        commands.add(new Command(
            Pattern.compile(cutEnd(from)+';',Pattern.LITERAL),
            cutEnd(to)+';'));
        // slash-delimited pattern "org/acme/foo/"
        commands.add(new Command(
            Pattern.compile(from.replace('.','/'),Pattern.LITERAL),
            to.replace('.','/')));
        // backslash-delimited pattern "org\acme\foo\"
        commands.add(new Command(
            Pattern.compile(from.replace('.','\\'),Pattern.LITERAL),
            to.replace('.','\\')));
    }

    private String cutEnd(String from) {
        return from.substring(0,from.length()-1);
    }

    /**
     * Converts the relative path if the given path matches this rename pattern.
     *
     * For example, "org/acme/foo/abc/Foo.java" will become
     * "org/acme/internal/foo/abc/Foo.java"
     *
     * @return
     *      Null if the path name doesn't match this rename command.
     */
    String convertPath(String relPath) {
        String norm = relPath.replace('/','.').replace('\\','.');
        if(norm.startsWith(from))
            return to.replace('.',File.separatorChar)+relPath.substring(from.length());
        else
            return null;
    }
}
