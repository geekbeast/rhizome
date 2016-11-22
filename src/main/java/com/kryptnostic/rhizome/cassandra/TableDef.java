package com.kryptnostic.rhizome.cassandra;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
public interface TableDef {
    String getKeyspace();
    String getName();
}
