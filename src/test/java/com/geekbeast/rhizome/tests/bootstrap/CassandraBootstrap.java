package com.geekbeast.rhizome.tests.bootstrap;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraBootstrap {
    private static Logger logger = LoggerFactory.getLogger( CassandraBootstrap.class );

    @BeforeClass
    public static void startCassandra() throws ConfigurationException, TTransportException, IOException {
        EmbeddedCassandraServerHelper
                .startEmbeddedCassandra( "cu-cassandra-rndport-workaround.yaml" );
        logger.info( "Started cassandra on port: {}", EmbeddedCassandraServerHelper.getNativeTransportPort() );
    }

    @Test
    public void foo() throws InterruptedException {
    }
}
