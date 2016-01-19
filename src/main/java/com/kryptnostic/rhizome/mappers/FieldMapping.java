package com.kryptnostic.rhizome.mappers;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * @param <T> Type to be mapped.
 */
public class FieldMapping<T> {
    private final Class<T>                    clazz;
    private final Map<String, BasicFieldType> types;

    public FieldMapping( Class<T> clazz, Map<String, BasicFieldType> types ) {
        this.clazz = clazz;
        this.types = types;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public Map<String, BasicFieldType> getTypes() {
        return types;
    }

    public static enum BasicFieldType {
        UUID( "uuid" ),
        INT64( "bigint" ),
        STRING( "text" ),
        BYTES( "blob" ),
        TIMEUUID( "timeuuid" );

        private final String cql;

        private BasicFieldType( String cql ) {
            this.cql = cql;
        }

        public String getCql() {
            return cql;
        }
    }

    public String getCql() {
        String cql = "(";
        int i = 0;

        for ( Entry<String, BasicFieldType> entry : types.entrySet() ) {
            cql += entry.getKey() + " " + entry.getValue().getCql() + ( types.size() == ++i ? ")" : "," );
        }

        return cql;
    }
}
