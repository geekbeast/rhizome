package com.kryptnostic.rhizome.mapstores.cassandra;

import com.datastax.driver.core.Cluster;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.geekbeast.rhizome.pods.RegistryBasedMappersPod;
import com.kryptnostic.rhizome.cassandra.BaseCassandraMapStore;
import com.kryptnostic.rhizome.cassandra.CassandraMapper;
import com.kryptnostic.rhizome.cassandra.SimpleCassandraMapper;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.AbstractMapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.KryptnosticMapStoreFactory;
import com.kryptnostic.rhizome.mapstores.MapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

public class CassandraMapStoreFactory implements KryptnosticMapStoreFactory {

    final Cluster                cluster;
    final CassandraConfiguration config;

    CassandraMapStoreFactory(
            Cluster cluster,
            CassandraConfiguration config ) {
        super();
        this.cluster = cluster;
        this.config = config;
    }

    @Override
    public <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) RegistryBasedMappersPod.getKeyMapper( keyType );
        CassandraMapper<V> valueMapper = new SimpleCassandraMapper<>( valType );
        return new CassandraMapStoreBuilder<>( keyMapper, valueMapper );
    }

    public class CassandraMapStoreBuilder<K, V> extends AbstractMapStoreBuilder<K, V> {

        public CassandraMapStoreBuilder(
                KeyMapper<K> keyMapper,
                CassandraMapper<V> valueMapper ) {
            super( keyMapper, valueMapper );
        }

        @Override
        public TestableSelfRegisteringMapStore<K, V> build() {
            return new BaseCassandraMapStore<K, V>(
                    tableName,
                    mapName,
                    keyMapper,
                    (CassandraMapper<V>) valueMapper,
                    config,
                    cluster) {
                @Override
                public K generateTestKey() {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME drew" );
                }

                @Override
                public V generateTestValue() throws Exception {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME drew" );
                }
            };
        }

    }

    public static class Builder {

        private CassandraConfiguration config;
        private Cluster                cluster;

        public Builder() {}

        public Builder withConfiguration( CassandraConfiguration config ) {
            this.config = config;
            return this;
        }

        public Builder withCluster( Cluster cluster ) {
            this.cluster = cluster;
            return this;
        }

        public CassandraMapStoreFactory build() {
            return new CassandraMapStoreFactory( cluster, config );
        }
    }

}
