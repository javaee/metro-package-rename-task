package org.acme.foo;

import org.acme.foo.us.SanFrancisco;
// wildcard import
import org.acme.foo.japan.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class City extends SanFrancisco {
    private org.acme.foo.us.ca.Springfield ca;
    private org.acme.foo.us.co.Springfield co;

    private Osaka o;
    private Tokyo t;

    // can we rename two things in one line?
    public void test(org.acme.foo.us.ca.Springfield x, org.acme.foo.us.ca.Springfield y) {
        // how about string
        System.out.println("org.acme.foo.us.SanFrancisco");
        System.out.println("Lorg/acme/foo/us/SanFrancisco;");

        // literal
        Class cl = org.acme.foo.us.co.Springfield.class;
    }
}
