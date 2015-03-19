package com.kryptnostic.rhizome.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.hazelcast.core.MapStore;

public class BaseCassandraMapStore<K, V> implements MapStore<K, V> {
    private final Cluster       cluster;
    private static final Logger logger           = LoggerFactory.getLogger( BaseCassandraMapStore.class );
    private static final String KEYSPACE_QUERY   = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String TABLE_QUERY      = "CREATE TABLE IF NOT EXISTS %s.%s ("
                                                         + "id uuid PRIMARY KEY," + "title text," + "album text,"
                                                         + "artist text," + "tags set<text>," + "data blob" + ");";
    private static final String LOAD_QUERY       = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String STORE_QUERY      = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String DELETE_QUERY     = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String LOAD_ALL_QUERY   = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String DELETE_ALL_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";

    public BaseCassandraMapStore( String space, String table, CassandraConfiguration configuration ) {
        Cluster.Builder b = Cluster.builder();
        configuration.getCassandraSeedNodes().forEach( ( node ) -> b.addContactPoint( node ) );
        cluster = b.build();

        Metadata metadata = cluster.getMetadata();
        logger.info( "Connected to cluster: {}", metadata.getClusterName() );
        for ( Host host : metadata.getAllHosts() ) {
            logger.info(
                    "Datacenter: {}; Host: {}; Rack: {}\n",
                    host.getDatacenter(),
                    host.getAddress(),
                    host.getRack() );
        }
        Session session = cluster.newSession();
        session.execute( String.format( KEYSPACE_QUERY, space ) );
        session.execute( String.format( TABLE_QUERY, space , table ) );
    }

    @Override
    public V load( K key ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<K> loadAllKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void store( K key, V value ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void storeAll( Map<K, V> map ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete( K key ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        // TODO Auto-generated method stub

    }

}
