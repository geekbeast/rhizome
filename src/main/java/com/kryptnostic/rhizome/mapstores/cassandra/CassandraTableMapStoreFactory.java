package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mappers.FieldMapping;
import com.kryptnostic.rhizome.pods.RegistryBasedMappersPod;

public class CassandraTableMapStoreFactory {
    private static final String KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = %s;";
    private static final String TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (%s text, %s %s, PRIMARY KEY( %s, %s ) );";
    private final Builder       builder;

    private CassandraTableMapStoreFactory( Builder builder ) {
        this.builder = builder;
        createKeyspace( builder.session, builder.keyspace, builder.replicationStrategy );
    }

    private static void createKeyspace( Session session, String keyspace, ReplicationStrategy replicationStrategy ) {
        session.execute(
                String.format( KEYSPACE_QUERY, keyspace, replicationStrategy.getCqlString() ) );
    }

    private static void createTable( Session session, String keyspace, String table, FieldMapping<?> partitionKeys, Map<String,FieldMapping<?>> clusteringKeys, Map<String,FieldMapping<?>> values ) {
        String PARTION_KEYS = partitionKeys.getCql();
      
    }

    public static Builder newBuilder( String keyspace, String table ) {
        return new Builder().usingKeySpace( keyspace ).withTableName( table );
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final AtomicBoolean  finalized            = new AtomicBoolean( false );
        String                       keyspace;
        String                       table;
        Session                      session;
        CassandraConfiguration       config;
        RegistryBasedMappersPod      mappers;
        ReplicationStrategy          replicationStrategy  = new SimpleStrategy( 2 );
        FieldMapping<?>              partitionKey;
        Map<String, FieldMapping<?>> clusteringKeyClasses = Maps.newLinkedHashMap();
        Map<String, FieldMapping<?>> valueClasses         = Maps.newLinkedHashMap();

        EnumSet<Components>          requiredComponents   = EnumSet.noneOf( Components.class );

        public Builder() {}

        public Builder usingKeySpace( String keyspace ) {
            ensureNotFinalized();
            Preconditions.checkArgument( StringUtils.isNotBlank( keyspace ), "Keyspace name cannot be blank." );
            this.keyspace = keyspace;
            requiredComponents.add( Components.KEYSPACE );
            return this;
        }

        public Builder withTableName( String table ) {
            ensureNotFinalized();
            Preconditions.checkArgument( StringUtils.isNotBlank( table ), "Table name cannot be blank." );
            this.table = table;
            requiredComponents.add( Components.TABLE_NAME );
            return this;
        }

        public Builder withPartitionKeyTypes( FieldMapping<?> mapping ) {
            ensureNotFinalized();
            this.partitionKey = Preconditions.checkNotNull( mapping, "Partition key mapping must not be null." );
            requiredComponents.add( Components.PARTITION_KEY );
            return this;
        }

        public Builder withClusteringKeyTypes( Map<String, FieldMapping<?>> classes ) {
            ensureNotFinalized();
            Preconditions.checkArgument( classes.size() > 0, "At least one clustering key type must be specified." );
            this.clusteringKeyClasses.putAll( classes );
            requiredComponents.add( Components.CLUSTERING_KEY );
            return this;
        }

        public Builder withValueTypes( Map<String, FieldMapping<?>> classes ) {
            ensureNotFinalized();
            Preconditions.checkArgument( classes.size() > 0, "At least one value type must be specified." );
            this.valueClasses.putAll( classes );
            return this;
        }

        public Builder withConfiguration( CassandraConfiguration configuration ) {
            ensureNotFinalized();
            this.config = Preconditions.checkNotNull( configuration, "Configuration cannot be null" );
            requiredComponents.add( Components.CONFIGURATION );
            return this;
        }

        public Builder usingSession( Session session ) {
            ensureNotFinalized();
            this.session = Preconditions.checkNotNull( session, "Session cannot be null." );
            requiredComponents.add( Components.SESSION );
            return this;
        }

        public Builder withMappers( RegistryBasedMappersPod mappers ) {
            ensureNotFinalized();
            this.mappers = Preconditions.checkNotNull( mappers, "Configuration cannot be null" );
            requiredComponents.add( Components.MAPPERS );
            return this;
        }

        public Builder withReplicationFactor( ReplicationStrategy replicationStrategy ) {
            ensureNotFinalized();
            this.replicationStrategy = Preconditions.checkNotNull( replicationStrategy,
                    "Configuration cannot be null" );
            requiredComponents.add( Components.MAPPERS );
            return this;
        }

        public CassandraTableMapStoreFactory build() {
            EnumSet<Components> missingComponents = EnumSet.complementOf( requiredComponents );
            Preconditions.checkState( missingComponents.isEmpty(),
                    "Missing required components: " + missingComponents.toString() );
            // Once validatons passes no more changes will be allowed even across threads.
            // Calling build multiple times is fine.
            finalized.set( true );
            return new CassandraTableMapStoreFactory( this );
        }

        private void ensureNotFinalized() {
            Preconditions.checkState( !finalized.get(), "Builder has been finalized and cannot be modified." );
        }

        static enum Components {
            KEYSPACE,
            TABLE_NAME,
            PARTITION_KEY,
            CLUSTERING_KEY,
            SESSION,
            CONFIGURATION,
            MAPPERS
        }
    }
}
