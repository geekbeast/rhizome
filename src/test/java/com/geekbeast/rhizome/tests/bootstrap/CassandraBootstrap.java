package com.geekbeast.rhizome.tests.bootstrap;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kryptnostic.rhizome.pods.CassandraPod;

public class CassandraBootstrap {
    private static Logger     logger = LoggerFactory.getLogger( CassandraBootstrap.class );
    private static final Lock lock   = new ReentrantLock();
    static {
        CassandraPod.setEmbeddedCassandraManager( EmbeddedCassandraServerHelper::startEmbeddedCassandra );
    }

    @BeforeClass
    public static void startCassandra() throws ConfigurationException, TTransportException, IOException {
        if ( lock.tryLock() ) {
            EmbeddedCassandraServerHelper
                    .startEmbeddedCassandra( CassandraPod.TEST_YAML );
            logger.info( "Started cassandra on port: {}", EmbeddedCassandraServerHelper.getNativeTransportPort() );
        }
    }

    @Test
    public void foo() throws InterruptedException {}
}
