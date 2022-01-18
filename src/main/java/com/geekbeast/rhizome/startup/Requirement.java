package com.geekbeast.rhizome.startup;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public interface Requirement {
    String getDescription();
    boolean isSatisfied();
}
