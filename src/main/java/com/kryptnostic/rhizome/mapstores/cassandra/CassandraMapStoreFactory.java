package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Set;

import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.geekbeast.rhizome.pods.RegistryBasedMappersPod;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.AbstractMapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.KryptnosticMapStoreFactory;
import com.kryptnostic.rhizome.mapstores.MapStoreBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

public class CassandraMapStoreFactory implements KryptnosticMapStoreFactory {

    final Session                session;
    final CassandraConfiguration config;

    CassandraMapStoreFactory( Builder builder ) {
        super();
        this.session = builder.getSession();
        this.config = builder.getConfig();
    }

    @Override
    public <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) RegistryBasedMappersPod.getKeyMapper( keyType );
        ValueMapper<V> valueMapper = (ValueMapper<V>) RegistryBasedMappersPod.getValueMapper( valType );
        return new CassandraMapStoreBuilder<>( keyMapper, valueMapper );
    }

    @Override
    public <K, C extends Set<V>, V> MapStoreBuilder<K, C> buildSetProxy( Class<K> keyType, Class<V> valType ) {
        KeyMapper<K> keyMapper = (KeyMapper<K>) RegistryBasedMappersPod.getKeyMapper( keyType );
        ValueMapper<V> valueMapper = (ValueMapper<V>) RegistryBasedMappersPod.getValueMapper( valType );
        // create a SetProxyAwareValueMapper<C> that wraps valueMapper<V>
        return new ProxiedCassandraMapStoreBuilder<>( keyMapper, valueMapper );
    }

    public class ProxiedCassandraMapStoreBuilder<K, C extends Set<V>, V> extends CassandraMapStoreBuilder<K, C> {

        private final ValueMapper<V> innerValueMapper;

        public ProxiedCassandraMapStoreBuilder( KeyMapper<K> keyMapper, ValueMapper<V> valueMapper ) {
            super( keyMapper, new SetProxyAwareValueMapper<C, V>( valueMapper ) );
            this.innerValueMapper = valueMapper;
        }

        @Override
        public TestableSelfRegisteringMapStore<K, C> build() {
            return new SetProxyBackedCassandraMapStore<K, C, V>(
                    tableName,
                    mapName,
                    keyMapper,
                    innerValueMapper,
                    config,
                    session );
        }
    }

    public class CassandraMapStoreBuilder<K, C> extends AbstractMapStoreBuilder<K, C> {

        public CassandraMapStoreBuilder(
                KeyMapper<K> keyMapper,
                ValueMapper<C> valueMapper ) {
            super( keyMapper, valueMapper );
        }

        @Override
        public TestableSelfRegisteringMapStore<K, C> build() {
            return new BaseCassandraMapStore<K, C>(
                    tableName,
                    mapName,
                    keyMapper,
                    valueMapper,
                    config,
                    session) {
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
        }

    }

    public static class Builder {

        private CassandraConfiguration config;
        private Session                session;

        public Builder() {}

        public Builder withConfiguration( CassandraConfiguration config ) {
            this.config = config;
            return this;
        }

        public Builder withSession( Session cluster ) {
            this.session = cluster;
            return this;
        }

        public CassandraMapStoreFactory build() {
            return new CassandraMapStoreFactory( this );
        }

        /**
         * @return the config
         */
        public CassandraConfiguration getConfig() {
            return config;
        }

        /**
         * @return the session
         */
        public Session getSession() {
            return session;
        }
    }

}
