package com.kryptnostic.rhizome.configuration.spark;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kryptnostic.rhizome.configuration.amazon.AmazonConfiguration;

public class SparkConfiguration {

    private static final String       SPARK_PORT_PROPERTY     = "port";
    private static final String       SPARK_MASTERS_PROPERTY  = "master";
    private static final String       SPARK_APP_NAME_PROPERTY = "appname";
    private static final String       JAR_LOCATIONS_PROPERTY  = "jars";
    private static final String       LOCAL_PROPERTY          = "local";
    private static final String       WORKING_DIR_PROPERTY    = "workingDir";

    private static final int          PORT_DEFAULT            = 7077;
    private static final boolean      LOCAL_DEFAULT           = true;
    private static final String       APP_NAME_DEFAULT        = "Test Rhizome App";
    private static final List<String> MASTER_DEFAULT          = ImmutableList.of( "127.0.0.1" );
    private static final List<String> JAR_LOCATIONS_DEFAULT   = ImmutableList.of();
    private static final String       WORKING_DIR_DEFAULT     = "/sparkWorkingDir";

    private final boolean             local;
    private final int                 sparkPort;
    private final String              appName;
    private final List<String>        sparkMasters;
    private final List<String>        jarLocations;
    private String                    provider;
    private String                    region;
    private String                    sparkWorkingDirectory;

    private static final Logger       logger                  = LoggerFactory
            .getLogger( SparkConfiguration.class );

    @JsonCreator
    public SparkConfiguration(
            @JsonProperty( SPARK_MASTERS_PROPERTY ) Optional<List<String>> masters,
            @JsonProperty( JAR_LOCATIONS_PROPERTY ) Optional<List<String>> jars,
            @JsonProperty( SPARK_APP_NAME_PROPERTY ) Optional<String> app,
            @JsonProperty( SPARK_PORT_PROPERTY ) Optional<Integer> port,
            @JsonProperty( LOCAL_PROPERTY ) Optional<Boolean> local,
            @JsonProperty( WORKING_DIR_PROPERTY ) Optional<String> workingDir,
            @JsonProperty( AmazonConfiguration.PROVIDER_PROPERTY ) Optional<String> provider,
            @JsonProperty( AmazonConfiguration.AWS_REGION_PROPERTY ) Optional<String> region,
            @JsonProperty( AmazonConfiguration.AWS_NODE_TAG_KEY_PROPERTY ) Optional<String> tagKey,
            @JsonProperty( AmazonConfiguration.AWS_NODE_TAG_VALUE_PROPERTY ) Optional<String> tagValue ) {
        this.sparkPort = port.or( PORT_DEFAULT );
        this.appName = app.or( APP_NAME_DEFAULT );
        this.jarLocations = jars.or( JAR_LOCATIONS_DEFAULT );
        this.local = local.or( LOCAL_DEFAULT );
        this.sparkWorkingDirectory = workingDir.or( WORKING_DIR_DEFAULT );

        this.provider = provider.orNull();
        if ( "aws".equalsIgnoreCase( provider.get() ) ) {
            this.sparkMasters = Lists.transform(
                    AmazonConfiguration.getNodesWithTagKeyAndValueInRegion( this.region,
                            tagKey,
                            tagValue,
                            logger ),
                    ( input ) -> input.getHostAddress() );
            this.region = region.or( AmazonConfiguration.AWS_REGION_DEFAULT );
        } else {
            this.sparkMasters = masters.or( MASTER_DEFAULT );
        }
    }

    @JsonProperty( AmazonConfiguration.PROVIDER_PROPERTY )
    public String getProvider() {
        return provider;
    }

    @JsonProperty( AmazonConfiguration.AWS_REGION_PROPERTY )
    public String getAwsRegion() {
        return region;
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
        Preconditions.checkState( jarLocations.stream().allMatch( StringUtils::isNotBlank ), "Invalid jars provided." );
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

    @JsonProperty( WORKING_DIR_PROPERTY )
    public String getWorkingDirectory() {
        return sparkWorkingDirectory;
    }
}
