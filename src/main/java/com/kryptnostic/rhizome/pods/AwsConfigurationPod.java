package com.kryptnostic.rhizome.pods;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.openlattice.ResourceConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Used for bootstrapping configuration from a (secure) S3 bucket.
 */
@Import( AwsRhizomeConfigurationPod.class )
@Profile( { Profiles.AWS_CONFIGURATION_PROFILE, Profiles.AWS_TESTING_PROFILE } )
@Configuration
public class AwsConfigurationPod {
    private static final Logger   logger = LoggerFactory.getLogger( ConfigurationPod.class );
    private static final AmazonS3 s3     = new AmazonS3Client();

    @Bean
    @Profile( Profiles.AWS_CONFIGURATION_PROFILE )
    public AmazonLaunchConfiguration awsConfig() {
        final AmazonLaunchConfiguration awsConfig = ResourceConfigurationLoader
                .loadConfigurationFromResource( "aws.yaml", AmazonLaunchConfiguration.class );
        logger.info( "Using aws configuration: {}", awsConfig );
        return awsConfig;
    }

    @Bean
    @Profile( Profiles.AWS_TESTING_PROFILE )
    public AmazonLaunchConfiguration awsTestingConfig() {
        final AmazonLaunchConfiguration awsConfig = ResourceConfigurationLoader
                .loadConfigurationFromResource( "awstest.yaml", AmazonLaunchConfiguration.class );
        logger.info( "Using aws configuration: {}", awsConfig );
        return awsConfig;
    }

    @Bean
    public AmazonS3 s3() {
        return s3;
    }

}
