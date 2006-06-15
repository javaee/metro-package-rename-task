package com.sun.wts.tools.ant;

import java.util.regex.Pattern;

/**
 * One replace command.
 *
 * @author Kohsuke Kawaguchi
 */
public class Command {
    private final Pattern from;
    private final String to;

    public Command(Pattern from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Replaces all the occurrences of the given pattern and returns it.
     */
    public String replace(String input) {
        return from.matcher(input).replaceAll(to);
    }
}
