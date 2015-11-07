package com.kryptnostic.rhizome.mapstores;

import com.datastax.driver.core.Cluster;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.geekbeast.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.kryptnostic.rhizome.cassandra.BaseCassandraMapStore;
import com.kryptnostic.rhizome.cassandra.CassandraMapper;
import com.kryptnostic.rhizome.cassandra.SimpleCassandraMapper;
import com.kryptnostic.rhizome.mappers.KeyMapper;

public class CassandraMapStoreFactory {

    private final Cluster                cluster;
    private final CassandraConfiguration config;
    private final String                 mapName;
    private final String                 table;

    CassandraMapStoreFactory(
            Cluster cluster,
            CassandraConfiguration config,
            String mapName,
            String table ) {
        super();
        this.cluster = cluster;
        this.config = config;
        this.mapName = mapName;
        this.table = table;
    }

    public <K, V> BaseCassandraMapStore<K, V> getMapstore( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) RegistryBasedHazelcastInstanceConfigurationPod.getKeyMapper( keyType );
        CassandraMapper<V> valueMapper = new SimpleCassandraMapper<V>( valType );
        return new BaseCassandraMapStore<K, V>(
                table,
                mapName,
                keyMapper,
                valueMapper,
                config,
                cluster ) { /* No-op */ };
    }

    public static class Builder {

        private String                 table;
        private String                 mapName;
        private CassandraConfiguration config;
        private Cluster                cluster;

        public Builder() {}

        public Builder withTable( String table ) {
            this.table = table;
            return this;
        }

        public Builder withMapName( String mapName ) {
            this.mapName = mapName;
            return this;
        }

        public Builder withConfiguration( CassandraConfiguration config ) {
            this.config = config;
            return this;
        }

        public Builder withCluster( Cluster cluster ) {
            this.cluster = cluster;
            return this;
        }

        public CassandraMapStoreFactory build() {
            return new CassandraMapStoreFactory( cluster, config, mapName, table );
        }

        public Builder withTableAndMapName( String name ) {
            this.table = name;
            this.mapName = name;
            return this;
        }
    }
}
