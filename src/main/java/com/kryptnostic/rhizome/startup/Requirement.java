package com.kryptnostic.rhizome.startup;

import java.util.function.Predicate;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public interface Requirement {
    String getDescription();
    boolean isSatisfied();
}
