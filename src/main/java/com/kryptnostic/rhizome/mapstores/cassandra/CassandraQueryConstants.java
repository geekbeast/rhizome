package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.UUID;

public class CassandraQueryConstants {

    static final String COUNT_RESULT_COLUMN_NAME = "count";
    static final String VALUE_RESULT_COLUMN_NAME = "results";
    private static final String TEXT_TYPE                = "text";
    private static final String BLOB_TYPE                = "blob";
    private static final String UUID_TYPE                = "uuid";
    private static final String BOOLEAN_TYPE             = "boolean";

    static String cassandraType( Class type ) {
        if ( type.equals( byte[].class ) ) {
            return BLOB_TYPE;
        } else if ( type.equals( boolean.class ) ) {
            return BOOLEAN_TYPE;
        } else if ( type.equals( UUID.class ) ) {
            return UUID_TYPE;
        }
        return TEXT_TYPE;
    }
}
