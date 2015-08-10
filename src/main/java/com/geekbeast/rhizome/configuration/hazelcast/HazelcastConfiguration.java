package com.geekbeast.rhizome.configuration.hazelcast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class HazelcastConfiguration {
    private static final String       REPLICATION_FACTOR         = "replication-factor";
    private static final String       SEED_NODES_PROPERTY        = "seed-nodes";
    private static final String       NAME_PROPERTY              = "instance-name";
    private static final String       GROUP_PROPERTY             = "group";
    private static final String       PASSWORD_PROPERTY          = "password";
    private static final String       PORT_PROPERTY              = "port";

    public static final String        DEFAULT_INSTANCE_NAME      = "rhizome-default";
    public static final String        DEFAULT_GROUP_NAME         = "rhizome-dev";
    public static final String        DEFAULT_PASSWORD           = "reticulating splines";
    public static final int           DEFAULT_PORT               = 5701;

    private static final List<String> SEED_DEFAULT               = ImmutableList.of( "127.0.0.1" );
    private static final int          REPLICATION_FACTOR_DEFAULT = 2;

    private final List<String>        hazelcastSeedNodes;
    private final int                 replicationFactor;
    private final String              instanceName;
    private final String              group;
    private final String              password;
    private final int                 port;

    @JsonCreator
    public HazelcastConfiguration(
            @JsonProperty( NAME_PROPERTY ) Optional<String> instanceName,
            @JsonProperty( GROUP_PROPERTY ) Optional<String> group,
            @JsonProperty( PASSWORD_PROPERTY ) Optional<String> password,
            @JsonProperty( PORT_PROPERTY ) Optional<Integer> port,
            @JsonProperty( SEED_NODES_PROPERTY ) Optional<List<String>> hazelcastSeedNodes,
            @JsonProperty( REPLICATION_FACTOR ) Optional<Integer> replicationFactor ) {

        this.group = group.or( DEFAULT_GROUP_NAME );
        this.password = group.or( DEFAULT_PASSWORD );
        this.port = port.or( DEFAULT_PORT );
        this.hazelcastSeedNodes = hazelcastSeedNodes.or( SEED_DEFAULT );
        this.replicationFactor = replicationFactor.or( REPLICATION_FACTOR_DEFAULT );
        this.instanceName = instanceName.or( DEFAULT_INSTANCE_NAME );
    }

    @JsonProperty( SEED_NODES_PROPERTY )
    public List<String> getHazelcastSeedNodes() {
        return hazelcastSeedNodes;
    }

    @JsonProperty( REPLICATION_FACTOR )
    public int getReplicationFactor() {
        return replicationFactor;
    }

    @JsonProperty( NAME_PROPERTY )
    public String getInstanceName() {
        return instanceName;
    }

    @JsonProperty( GROUP_PROPERTY )
    public String getGroup() {
        return group;
    }

    @JsonProperty( PASSWORD_PROPERTY )
    public String getPassword() {
        return password;
    }

    @JsonProperty( PORT_PROPERTY )
    public int getPort() {
        return port;
    }

}
