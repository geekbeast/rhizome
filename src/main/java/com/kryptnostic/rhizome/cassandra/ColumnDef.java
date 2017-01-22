package com.kryptnostic.rhizome.cassandra;

import java.util.function.Function;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.BindMarker;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public interface ColumnDef {
    /**
     * @return A valid CQL column name.
     */
    String cql();

    DataType getType( Function<ColumnDef, DataType> typeResolver );

    DataType getType();

    /**
     * @return Returns a bind marker. The default implementation returns an anonymous marker.
     */
    default BindMarker bindMarker() {
        return QueryBuilder.bindMarker();
    }

    default Clause eq() {
        return QueryBuilder.eq( cql(), bindMarker() );
    }
}
