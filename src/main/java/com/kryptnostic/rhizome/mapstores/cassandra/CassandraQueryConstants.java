package com.kryptnostic.rhizome.mapstores.cassandra;

public class CassandraQueryConstants {

    static final String COUNT_RESULT_COLUMN_NAME = "count";
    static final String VALUE_RESULT_COLUMN_NAME = "results";

    private static final String TEXT_TYPE                = "text";
    private static final String BLOB_TYPE                = "blob";
    private static final String UUID_TYPE                = "uuid";
    private static final String BOOLEAN_TYPE             = "boolean";

    static String cassandraValueType( Class type ) {
        // Currently using valuemappers that write everything as binary
        // We should probably think about that more with the
        // new cassandra stuff since it's way way way way better
        return BLOB_TYPE;
    }
}
