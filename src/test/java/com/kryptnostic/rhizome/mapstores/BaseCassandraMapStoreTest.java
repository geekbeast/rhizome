package com.kryptnostic.rhizome.mapstores;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kryptnostic.rhizome.mapstores.CassandraMapStoreFactory.Builder;


/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * This test requires a cassandra instance to be locally available in order to run thest.
 */
@Ignore
public class BaseCassandraMapStoreTest {

    private final CassandraConfiguration config = new CassandraConfiguration(
                        Optional.of( false ),
                        Optional.of( ImmutableList.of( "localhost" ) ),
                        Optional.of( "test" ),
                        Optional.of( 3 ) );

    @Test
    public void testCassandraMapstore() {

        Cluster clust = new Cluster.Builder()
            .addContactPoints( config.getCassandraSeedNodes() )
            .build();

        TestableSelfRegisteringMapStore<String, String> store = new Builder()
                .withCluster( clust )
                .withConfiguration( config )
                .build().getMapStoreBuilder( String.class, String.class )
                .withMapName( "test" )
                .withTableName( "test" )
                .build();

        store.store( "blah", "humbugabcdef" );
        Assert.assertEquals( "humbugabcdef", store.load( "blah" ) );
        store.delete( "blah" );
        Assert.assertEquals( null, store.load( "blah" ) );

        store.store( "blah2", "humbugabcdef" );
        Assert.assertEquals( "humbugabcdef", store.load( "blah2" ) );
        store.deleteAll( ImmutableList.of( "blah2" ) );
        Assert.assertEquals( null, store.load( "blah2" ) );

        store.store( "blah", "humbugabcdef" );
        store.store( "blah2", "humbugabcdef" );
        Assert.assertEquals( ImmutableSet.of( "blah", "blah2" ), store.loadAllKeys() );
    }
}
