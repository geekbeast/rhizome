package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.AbstractMapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.KryptnosticMapStoreFactory;
import com.kryptnostic.rhizome.mapstores.MapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.mapstores.cassandra.SetProxyAwareValueMapper;
import com.kryptnostic.rhizome.pods.RegistryBasedMappersPod;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

/**
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
        SelfRegisteringKeyMapper<K> keyMapper = (SelfRegisteringKeyMapper<K>) mappers.getKeyMapper( keyType );
        SelfRegisteringValueMapper<V> valueMapper = (SelfRegisteringValueMapper<V>) mappers.getValueMapper( valType );
        Preconditions.checkNotNull( keyMapper, "No keymapper found for type %s ", keyType );
        Preconditions.checkNotNull( valueMapper, "No valuemapper found for type %s ", valType );
        return new ProxiedRethinkdbMapStoreBuilder<>( keyMapper, valueMapper, valType );
    }

    @Override
    public <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType ) {
        SelfRegisteringKeyMapper<K> keyMapper = (SelfRegisteringKeyMapper<K>) mappers.getKeyMapper( keyType );
        SelfRegisteringValueMapper<V> valueMapper = (SelfRegisteringValueMapper<V>) mappers.getValueMapper( valType );
        Preconditions.checkNotNull( keyMapper, "No keymapper found for type %s ", keyType );
        Preconditions.checkNotNull( valueMapper, "No valuemapper found for type %s ", valType );
        return new RethinkdbMapStoreBuilder<>( keyMapper, valueMapper );
    }

    public class ProxiedRethinkdbMapStoreBuilder<K, C extends Set<V>, V> extends RethinkdbMapStoreBuilder<K, C> {

        private final SelfRegisteringValueMapper<V> innerValueMapper;
        private final Class<V>                      valueType;

        public ProxiedRethinkdbMapStoreBuilder(
                SelfRegisteringKeyMapper<K> keyMapper,
                SelfRegisteringValueMapper<V> valueMapper,
                Class<V> valueType ) {
            super( keyMapper, new SetProxyAwareValueMapper<C, V>( valueMapper ) );
            this.innerValueMapper = valueMapper;
            this.valueType = valueType;
        }

        @Override
        public TestableSelfRegisteringMapStore<K, C> build() {
            RethinkDbBaseMapStoreAlternateDriver<K, C> rethinkDbBaseMapStoreAlternateDriver = new RethinkDbBaseMapStoreAlternateDriver<K, C>(
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
                public C generateTestValue() throws Exception {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME drew" );
                }
            };
            return rethinkDbBaseMapStoreAlternateDriver;
        }

    }

    public class RethinkdbMapStoreBuilder<K, V> extends AbstractMapStoreBuilder<K, V> {

        public RethinkdbMapStoreBuilder(
                SelfRegisteringKeyMapper<K> keyMapper,
                SelfRegisteringValueMapper<V> valueMapper ) {
            super( keyMapper, valueMapper );
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
