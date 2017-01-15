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

import com.datastax.driver.core.Cluster;
import com.kryptnostic.rhizome.cassandra.EmbeddedCassandraManager;
import com.kryptnostic.rhizome.pods.CassandraPod;

public class CassandraBootstrap {
    private static Logger     logger = LoggerFactory.getLogger( CassandraBootstrap.class );
    private static final Lock lock   = new ReentrantLock();
    static {
        CassandraPod.setEmbeddedCassandraManager( new RhizomeEmbeddedCassandraManager() );
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

    private static final class RhizomeEmbeddedCassandraManager implements EmbeddedCassandraManager {
        public void start( String yamlFile ) {
            try {
                EmbeddedCassandraServerHelper.startEmbeddedCassandra( yamlFile );
            } catch ( ConfigurationException | TTransportException | IOException e ) {
                throw new IllegalStateException( "Cassandra unable to start.", e );

            }
        }

        @Override
        public Cluster cluster() {
            return EmbeddedCassandraServerHelper.getCluster();
        }
    }
}
