package com.kryptnostic.rhizome.configuration.spark;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class SparkConfiguration {

    private static final String       SPARK_PORT_PROPERTY     = "port";
    private static final String       SPARK_MASTERS_PROPERTY  = "master";
    private static final String       SPARK_APP_NAME_PROPERTY = "appname";
    private static final String       JAR_LOCATIONS_PROPERTY  = "jars";
    private static final String       LOCAL_PROPERTY          = "local";

    private static final int          PORT_DEFAULT            = 7077;
    private static final boolean      LOCAL_DEFAULT           = true;
    private static final String       APP_NAME_DEFAULT        = "Test Rhizome App";
    private static final List<String> MASTER_DEFAULT          = ImmutableList.of( "127.0.0.1" );
    private static final List<String> JAR_LOCATIONS_DEFAULT   = ImmutableList.of( "" );

    private final boolean             local;
    private final int                 sparkPort;
    private final String              appName;
    private final List<String>        sparkMasters;
    private final List<String>        jarLocations;

    @JsonCreator
    public SparkConfiguration(
            @JsonProperty( SPARK_MASTERS_PROPERTY ) Optional<List<String>> masters,
            @JsonProperty( JAR_LOCATIONS_PROPERTY ) Optional<List<String>> jars,
            @JsonProperty( SPARK_APP_NAME_PROPERTY ) Optional<String> app,
            @JsonProperty( SPARK_PORT_PROPERTY ) Optional<Integer> port,
            @JsonProperty( LOCAL_PROPERTY ) Optional<Boolean> local ) {
        this.sparkPort = port.or( PORT_DEFAULT );
        this.sparkMasters = masters.or( MASTER_DEFAULT );
        this.appName = app.or( APP_NAME_DEFAULT );
        this.jarLocations = jars.or( JAR_LOCATIONS_DEFAULT );
        this.local = local.or( LOCAL_DEFAULT );
    }

    @JsonProperty( SPARK_PORT_PROPERTY )
    public int getSparkPort() {
        return sparkPort;
    }

    @JsonProperty( SPARK_MASTERS_PROPERTY )
    public List<String> getSparkMasters() {
        return sparkMasters;
    }

    @JsonProperty( JAR_LOCATIONS_PROPERTY )
    public String[] getJarLocations() {
        return jarLocations.toArray( new String[ jarLocations.size() ] );
    }

    @JsonProperty( SPARK_APP_NAME_PROPERTY )
    public String getAppName() {
        return appName;
    }

    @JsonProperty( LOCAL_PROPERTY )
    public boolean isLocal() {
        return local;
    }
}
