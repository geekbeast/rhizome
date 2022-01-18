package com.geekbeast.rhizome.pods;

import com.geekbeast.rhizome.configuration.ConfigurationConstants.Profiles;
import com.geekbeast.rhizome.configuration.configuration.amazon.AmazonLaunchConfiguration;
import com.geekbeast.rhizome.configuration.configuration.amazon.AwsLaunchConfiguration;
import com.geekbeast.ResourceConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Used for bootstrapping configuration from a (secure) S3 bucket.
 */
@Profile( { Profiles.AWS_CONFIGURATION_PROFILE, Profiles.AWS_TESTING_PROFILE } )
@Configuration
public class AwsConfigurationPod {
    private static final Logger logger = LoggerFactory.getLogger( ConfigurationPod.class );
    private static final AmazonLaunchConfiguration awsTestConfig;
    private static final AmazonLaunchConfiguration awsConfig;

    static {
        awsConfig = ResourceConfigurationLoader
                .loadConfigurationFromResource( "aws.yaml", AwsLaunchConfiguration.class );
        awsTestConfig = ResourceConfigurationLoader
                .loadConfigurationFromResource( "awstest.yaml", AwsLaunchConfiguration.class );
    }

    @Bean( name = "awsConfig" )
    @Profile( Profiles.AWS_CONFIGURATION_PROFILE )
    public AmazonLaunchConfiguration awsConfig() {
        logger.info( "Using aws configuration: {}", awsConfig );
        return awsConfig;
    }

    @Bean( name = "awsConfig" )
    @Profile( Profiles.AWS_TESTING_PROFILE )
    public AmazonLaunchConfiguration awsTestingConfig() {
        return awsTestConfig;
    }

}
