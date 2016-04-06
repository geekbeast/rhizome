package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.UUID;

import com.datastax.driver.core.DataType;

public class CassandraQueryConstants {

    static final String COUNT_RESULT_COLUMN_NAME = "count";

    static <T> String cassandraValueType( Class<T> type ) {
        // Currently using valuemappers that write everything as binary
        // We should probably think about cassandras built in datatypes
        // and whether we even need valuemappers
        if ( type.isAssignableFrom( UUID.class ) ) {
            return DataType.uuid().asFunctionParameterString();
        }
        return DataType.blob().asFunctionParameterString();
    }
}
