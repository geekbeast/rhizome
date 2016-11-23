package com.kryptnostic.rhizome.mapstores.cassandra;

import org.apache.commons.lang3.RandomStringUtils;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.cassandra.BindingFunction;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;
import com.kryptnostic.rhizome.cassandra.KeyBindingFunction;

public class StructuredCassandraMapstoreImpl extends AbstractStructuredCassandraMapstore<String, TestConfiguration>{

    public StructuredCassandraMapstoreImpl(
            String mapName,
            Session session,
            KeyBindingFunction<String> kf,
            BindingFunction<String, TestConfiguration> vsf,
            Function<Row, String> krf,
            Function<Row, TestConfiguration> vf,
            CassandraTableBuilder tableBuilder ) {
        super( mapName, session, kf, vsf, krf, vf, tableBuilder );
    }

    @Override
    public String generateTestKey() {
        return TestConfiguration.key().getUri();
    }

    @Override
    public TestConfiguration generateTestValue() throws Exception {
        return new TestConfiguration(
                RandomStringUtils.random( 10 ),
                Optional.<String> absent() );
    }

}
