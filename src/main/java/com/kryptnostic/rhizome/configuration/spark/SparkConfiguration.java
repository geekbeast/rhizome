package com.kryptnostic.rhizome.configuration.spark;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class SparkConfiguration {

    private static final String       SPARK_PORT_PROPERTY    = "port";
    private static final String       SPARK_MASTERS_PROPERTY = "master";

    private static final int          PORT_DEFAULT           = 7077;
    private static final List<String> MASTER_DEFAULT         = ImmutableList.of( "127.0.0.1" );

    private final int                 sparkPort;
    private final List<String>        sparkMasters;

    @JsonCreator
    public SparkConfiguration(
            @JsonProperty( SPARK_MASTERS_PROPERTY ) Optional<List<String>> masters,
            @JsonProperty( SPARK_PORT_PROPERTY ) Optional<Integer> port) {
        this.sparkPort = port.or( PORT_DEFAULT );
        this.sparkMasters = masters.or( MASTER_DEFAULT );
    }

    @JsonProperty( SPARK_PORT_PROPERTY )
    public int getSparkPort() {
        return sparkPort;
    }

    @JsonProperty( SPARK_MASTERS_PROPERTY )
    public List<String> getSparkMasters() {
        return sparkMasters;
    }
}
