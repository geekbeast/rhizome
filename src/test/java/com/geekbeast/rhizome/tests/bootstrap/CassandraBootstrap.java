package com.geekbeast.rhizome.tests.bootstrap;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.kryptnostic.rhizome.cassandra.EmbeddedCassandraManager;
import com.kryptnostic.rhizome.pods.CassandraPod;

@Ignore
public class CassandraBootstrap {
    private static final int                  EMBEDDED_TIMEOUT = 60000;

    private static final Logger                     logger           = LoggerFactory.getLogger( CassandraBootstrap.class );
    protected static EmbeddedCassandraManager ecm              = new RhizomeEmbeddedCassandraManager();
    static {
        CassandraPod.setEmbeddedCassandraManager( ecm );
    }

    @BeforeClass
    public static void startCassandra() throws ConfigurationException, TTransportException, IOException {
        ecm.start( CassandraPod.RANDOM_PORTS_YAML );
    }

    private static final class RhizomeEmbeddedCassandraManager implements EmbeddedCassandraManager {
        private static final Lock              lock            = new ReentrantLock();
        private static final Supplier<Cluster> clusterSupplier = Suppliers.memoize( () -> {
                                                                   final Cluster cluster = EmbeddedCassandraServerHelper
                                                                           .getCluster();
                                                                   cluster.getConfiguration()
                                                                           .getSocketOptions()
                                                                           .setReadTimeoutMillis( EMBEDDED_TIMEOUT );
                                                                   return cluster;
                                                               } );

        public void start( String yamlFile ) {
            if ( lock.tryLock() ) {
                try {
                    EmbeddedCassandraServerHelper.startEmbeddedCassandra( yamlFile, 100000 );
                    EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions()
                            .setReadTimeoutMillis( EMBEDDED_TIMEOUT );
                } catch ( ConfigurationException | TTransportException | IOException e ) {
                    throw new IllegalStateException( "Cassandra unable to start.", e );
                }
                logger.info( "Started cassandra on port: {}", EmbeddedCassandraServerHelper.getNativeTransportPort() );
            }
        }

        @Override
        public Cluster cluster() {
            return clusterSupplier.get();
        }
    }
}
