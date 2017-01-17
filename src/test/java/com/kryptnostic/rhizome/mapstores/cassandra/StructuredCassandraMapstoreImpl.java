package com.kryptnostic.rhizome.mapstores.cassandra;

import org.apache.commons.lang3.RandomStringUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;

public class StructuredCassandraMapstoreImpl extends AbstractStructuredCassandraMapstore<String, TestConfiguration> {

    public StructuredCassandraMapstoreImpl(
            String mapName,
            Session session,
            CassandraTableBuilder tableBuilder ) {
        super( mapName, session, tableBuilder );
    }

    @Override
    public String generateTestKey() {
        return TestConfiguration.key().getUri();
    }

    @Override
    public TestConfiguration generateTestValue() {
        return new TestConfiguration(
                RandomStringUtils.random( 10 ),
                Optional.<String> absent() );
    }

    @Override
    protected BoundStatement bind( String key, BoundStatement bs ) {
        return bs.set( "uri", key, String.class );
    }

    @Override
    protected BoundStatement bind( String key, TestConfiguration value, BoundStatement bs ) {
        return bs
                .set( "uri", key, String.class )
                .set( "required", value.getRequired(), String.class )
                .set( "optional", value.getOptional().orNull(), String.class );
    }

    @Override
    protected String mapKey( Row row ) {
        return row.getString( "uri" );
    }

    @Override
    protected TestConfiguration mapValue( ResultSet rs ) {
        Row row = rs.one();
        return row == null ? null
                : new TestConfiguration(
                        row.getString( "required" ),
                        Optional.fromNullable( row.getString( "optional" ) ) );
    }

}
