package com.kryptnostic.rhizome.mapstores;

import org.junit.Assert;
import org.junit.Test;

import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kryptnostic.rhizome.cassandra.BaseCassandraMapStore;
import com.kryptnostic.rhizome.cassandra.SimpleCassandraMapper;
import com.kryptnostic.rhizome.mappers.keys.StringKeyMapper;

public class CassandraMapstoreTester {
    @Test
    public void testCassandraMapstore() {
        BaseCassandraMapStore<String, String> store = new BaseCassandraMapStore<String, String>(
                "test",
                "test",
                new StringKeyMapper(),
                new SimpleCassandraMapper<String>( String.class ),
                new CassandraConfiguration(
                        Optional.of( false ),
                        Optional.of( ImmutableList.of( "localhost" ) ),
                        Optional.of( "test" ),
                        Optional.of( 3 ) ) );

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
