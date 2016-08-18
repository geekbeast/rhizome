package com.geekbeast.rhizome.tests.pods;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import com.geekbeast.rhizome.tests.bootstrap.EmbeddedCassandraServerHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfigurations;
import com.kryptnostic.rhizome.configuration.cassandra.Clusters;
import com.kryptnostic.rhizome.configuration.cassandra.Sessions;

@Configuration
@Profile( "cassandra-test" )
public class CassandraTestPod {
    private static final Logger  logger = LoggerFactory.getLogger( CassandraTestPod.class );

    @Inject
    private RhizomeConfiguration configuration;

    @Autowired(
        required = false )
    private Set<TypeCodec<?>>    codecs;

    @Bean
    public CassandraConfiguration cassandraConfiguration()
            throws ConfigurationException, TTransportException, IOException, URISyntaxException {
        try {
            EmbeddedCassandraServerHelper
                    .startEmbeddedCassandra();
        } catch ( InterruptedException e ) {
            logger.error( "Something strange in the neighborhood. ", e );
        }
        return new CassandraConfiguration(
                Optional.absent(),
                Optional.absent(),
                Optional.absent(),
                Optional.absent(),
                Optional.absent() );
    }

    @Bean
    public Session session() throws ConfigurationException, TTransportException, IOException, URISyntaxException {
        return cluster().newSession();
    }

    @Bean
    public CodecRegistry codecRegistry() {
        CodecRegistry registry = new CodecRegistry();
        if ( codecs != null ) {
            registry.register( codecs );
        }
        return registry;
    }

    @Bean
    public Cluster cluster() throws ConfigurationException, TTransportException, IOException, URISyntaxException {
        List<InetSocketAddress> contactPoints = cassandraConfiguration().getCassandraSeedNodes().stream()
                .map( node -> new InetSocketAddress( node, EmbeddedCassandraServerHelper.getNativeTransportPort() ) )
                .collect( Collectors.toList() );
        return new Cluster.Builder()
                .withClusterName( EmbeddedCassandraServerHelper.getClusterName() )
                .withCompression( cassandraConfiguration().getCompression() )
                .withPoolingOptions( getPoolingOptions() )
                .withProtocolVersion( ProtocolVersion.NEWEST_SUPPORTED )
                .addContactPointsWithPorts( contactPoints )
                .withCodecRegistry( codecRegistry() )
                .build();
    }

    @Bean
    public Clusters clusters() throws ConfigurationException, TTransportException, IOException, URISyntaxException {
        List<InetSocketAddress> contactPoints = cassandraConfiguration().getCassandraSeedNodes().stream()
                .map( node -> new InetSocketAddress( node, EmbeddedCassandraServerHelper.getNativeTransportPort() ) )
                .collect( Collectors.toList() );
        return new Clusters( Maps.transformValues( getConfigurations(),
                cassandraConfiguration -> new Cluster.Builder()
                        .withCompression( cassandraConfiguration.getCompression() )
                        .withClusterName( EmbeddedCassandraServerHelper.getClusterName() )
                        .withPoolingOptions( getPoolingOptions() )
                        .withProtocolVersion( ProtocolVersion.NEWEST_SUPPORTED )
                        .addContactPointsWithPorts( contactPoints )
                        .withCodecRegistry( codecRegistry() )
                        .build() ) );
    }

    @Bean
    public Sessions sessions() throws ConfigurationException, TTransportException, IOException, URISyntaxException {
        return new Sessions( Maps.transformValues( clusters(), cluster -> cluster.newSession() ) );
    }

    private static PoolingOptions getPoolingOptions() {
        PoolingOptions poolingOptions = new PoolingOptions();
        return poolingOptions;
    }

    @VisibleForTesting
    public void setRhizomeConfig( RhizomeConfiguration config ) {
        this.configuration = config;
    }

    @VisibleForTesting
    public void setCodecs( Set<TypeCodec<?>> codecs ) {
        this.codecs = codecs;
    }

    private CassandraConfigurations getConfigurations() {
        return configuration.getCassandraConfigurations().or( new CassandraConfigurations() );
    }

}
