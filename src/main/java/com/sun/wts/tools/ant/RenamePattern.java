package com.sun.wts.tools.ant;

import org.apache.maven.plugin.MojoFailureException;

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
     * Separator pattern for configuring target directory of the pattern.
     */
    private final String DIRECTORY_SEPARATOR = "/";
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
     * Directory to put renamed entries into for this pattern.
     */
    private String directory;

    /**
     * Package name that match these and the {@link #from} will be skipped.
     */
    private final List<String> excludes = new ArrayList<String>();


    public RenamePattern() {
    }

    public RenamePattern(String from, String to) {
        this.from = from;
        parseTo(to);
    }

    private void parseTo(String to) {
        if (!to.contains(DIRECTORY_SEPARATOR)) {
            this.to = to;
            return;
        }
        final int separatorIndex = to.lastIndexOf(DIRECTORY_SEPARATOR);
        this.directory = to.substring(0, separatorIndex);
        this.to = to.substring(separatorIndex + 1, to.length());
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

    static abstract class Function {
        abstract String apply(String s);
        String applyExclude(String s) { return apply(s); }
    }

    private static final Function[] FUNCTIONS = new Function[] {
        // dot-delimited pattern "org.acme.foo."
        new Function() {
            public String apply(String s) {
                return s;
            }
            String applyExclude(String s) {
                return cutEnd(s);
            }
        },
        // dot-delimited pattern with ';' termination. "org.acme.foo;"
        // used for package statement.
        new Function() {
            public String apply(String s) {
                return cutEnd(s) +';';
            }
        },
        // slash-delimited pattern "org/acme/foo/"
        new Function() {
            public String apply(String s) {
                return s.replace('.','/');
            }
        },
        // backslash-delimited pattern "org\acme\foo\"
        new Function() {
            public String apply(String s) {
                return s.replace('.','\\');
            }
        }
    };

    private static String cutEnd(String s) {
        return s.substring(0, s.length() - 1);
    }

    /**
     * Adds all the rename commands to the given list.
     */
    public void addCommands( List<Command> commands ) {

        for( Function f : FUNCTIONS ) {
            List<Pattern> exclusions = new ArrayList<Pattern>();
            for (String exclude : excludes)
                exclusions.add(Pattern.compile(f.applyExclude(exclude),Pattern.LITERAL));

            commands.add(new Command(
                Pattern.compile(f.apply(from),Pattern.LITERAL),
                f.apply(to),exclusions));
        }
    }

    /**
     * Converts the relative path if the given path matches this rename pattern.
     *
     * For example, "org/acme/foo/abc/Foo.java" will become
     * "org/acme/internal/foo/abc/Foo.java"
     *
     * @return
     *      Converted pattern
     */
    String convertPath(String relPath) {
        StringBuilder result = new StringBuilder();
        if (directory != null) {
            result.append(directory).append(File.separatorChar);
        }
        result.append(to.replace('.',File.separatorChar)).append(relPath.substring(from.length()));
        return result.toString();
    }

    /**
     * Checks if this pattern matches to given path.
     *
     * @param relPath path to match
     * @return true if matches
     */
    boolean matches(String relPath) {
        String norm = relPath.replace('/','.').replace('\\','.');
        if(norm.startsWith(from)) {
            for (String e : excludes) {
                if(norm.startsWith(e))
                    return false;
            }
            return true;
        }
        return false;
    }
}
