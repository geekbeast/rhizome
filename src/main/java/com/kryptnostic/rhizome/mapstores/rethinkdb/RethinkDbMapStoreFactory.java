package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Set;

import com.geekbeast.rhizome.pods.RegistryBasedMappersPod;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.AbstractMapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.KryptnosticMapStoreFactory;
import com.kryptnostic.rhizome.mapstores.MapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

/**
 *
 * @author Drew Bailey
 *
 */
public class RethinkDbMapStoreFactory implements KryptnosticMapStoreFactory {
    final RegistryBasedMappersPod            mappers;

    final RethinkDbAlternateDriverClientPool pool;
    final String                             dbName;

    RethinkDbMapStoreFactory( Builder builder ) {
        super();
        this.pool = builder.getPool();
        this.dbName = builder.getDbName();
        this.mappers = builder.getMappers();
    }

    @Override
    public <K, C extends Set<V>, V> MapStoreBuilder<K, C> buildSetProxy( Class<K> keyType, Class<V> valType ) {
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) mappers.getKeyMapper( keyType );
        ValueMapper<V> valueMapper = (ValueMapper<V>) mappers.getValueMapper( valType );
        if ( valueMapper == null ) {
            throw new RuntimeException( "There is no ValueMapper registered for type " + valType );
        }
        if ( keyMapper == null ) {
            throw new RuntimeException( "There is no KeyMapper registered for type " + keyType );
        }
        return new RethinkdbMapStoreBuilder<>( pool, dbName, keyMapper, valueMapper );
    }

    public static class RethinkdbMapStoreBuilder<K, V> extends AbstractMapStoreBuilder<K, V> {
        private final RethinkDbAlternateDriverClientPool pool;
        private final String                             dbName;

        public RethinkdbMapStoreBuilder(
                RethinkDbAlternateDriverClientPool pool,
                String dbName,
                KeyMapper<K> keyMapper,
                ValueMapper<V> valueMapper ) {
            super( keyMapper, valueMapper );
            this.dbName = dbName;
            this.pool = pool;
        }

        @Override
        public TestableSelfRegisteringMapStore<K, V> build() {
            RethinkDbBaseMapStoreAlternateDriver<K, V> rethinkDbBaseMapStoreAlternateDriver = new RethinkDbBaseMapStoreAlternateDriver<K, V>(
                    pool,
                    mapName,
                    dbName,
                    tableName,
                    keyMapper,
                    valueMapper) {

                @Override
                public MapConfig getMapConfig() {
                    MapConfig mapConfig = super.getMapConfig();
                    if ( objectFormat ) {
                        mapConfig.setInMemoryFormat( InMemoryFormat.OBJECT );
                    }
                    return mapConfig;
                }

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

            return rethinkDbBaseMapStoreAlternateDriver;
        }
    }

    public static class Builder {
        private RethinkDbAlternateDriverClientPool pool;
        private String                             dbName;
        private RegistryBasedMappersPod            mappers;

        public Builder() {}

        public Builder withPool( RethinkDbAlternateDriverClientPool pool ) {
            this.pool = pool;
            return this;
        }

        public Builder withMapper( RegistryBasedMappersPod mappers ) {
            this.mappers = mappers;
            return this;
        }

        public Builder withDbName( String name ) {
            this.dbName = name;
            return this;
        }

        /**
         * @return the pool
         */
        public RethinkDbAlternateDriverClientPool getPool() {
            return pool;
        }

        public RegistryBasedMappersPod getMappers() {
            return mappers;
        }
        
        /**
         * @return the dbName
         */
        public String getDbName() {
            return dbName;
        }

        public RethinkDbMapStoreFactory build() {
            return new RethinkDbMapStoreFactory( this );
        }

    }

}
