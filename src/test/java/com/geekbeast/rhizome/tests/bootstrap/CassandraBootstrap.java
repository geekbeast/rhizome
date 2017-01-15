package com.geekbeast.rhizome.tests.bootstrap;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.kryptnostic.rhizome.cassandra.EmbeddedCassandraManager;
import com.kryptnostic.rhizome.pods.CassandraPod;

public class CassandraBootstrap {
    private static Logger logger = LoggerFactory.getLogger( CassandraBootstrap.class );
    static {
        CassandraPod.setEmbeddedCassandraManager( new RhizomeEmbeddedCassandraManager() );
    }

    @BeforeClass
    public static void startCassandra() throws ConfigurationException, TTransportException, IOException {}

    private static final class RhizomeEmbeddedCassandraManager implements EmbeddedCassandraManager {
        private static final Lock lock = new ReentrantLock();

        public void start( String yamlFile ) {
            if ( lock.tryLock() ) {
                try {
                    EmbeddedCassandraServerHelper.startEmbeddedCassandra( yamlFile, 100000 );
                } catch ( ConfigurationException | TTransportException | IOException e ) {
                    throw new IllegalStateException( "Cassandra unable to start.", e );
                }
                logger.info( "Started cassandra on port: {}", EmbeddedCassandraServerHelper.getNativeTransportPort() );
            }
            startWithLongTimeout( yamlFile );
        }

        public static void startWithLongTimeout( String yamlFile ) {}

        @Override
        public Cluster cluster() {
            return EmbeddedCassandraServerHelper.getCluster();
        }
    }
}
