package com.kryptnostic.rhizome.cassandra;

import java.util.function.Function;

import com.datastax.driver.core.DataType;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
public interface ColumnDef {
    String cql();

    DataType getType( Function<ColumnDef, DataType> typeResolver );

    DataType getType();
}
