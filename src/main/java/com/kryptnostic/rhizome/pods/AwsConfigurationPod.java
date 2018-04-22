package com.kryptnostic.rhizome.pods;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.amazon.AwsLaunchConfiguration;
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
@Profile( { Profiles.AWS_CONFIGURATION_PROFILE, Profiles.AWS_TESTING_PROFILE } )
@Configuration
public class AwsConfigurationPod {
    private static final Logger   logger = LoggerFactory.getLogger( ConfigurationPod.class );
    private static final AmazonS3 s3     = new AmazonS3Client();
  private  static final AmazonLaunchConfiguration awsTestConfig;
  private  static final AmazonLaunchConfiguration awsConfig;
  static {
      awsConfig = ResourceConfigurationLoader
              .loadConfigurationFromResource( "aws.yaml", AwsLaunchConfiguration.class );
      awsTestConfig = ResourceConfigurationLoader
              .loadConfigurationFromResource( "awstest.yaml", AwsLaunchConfiguration.class );
  }
    @Bean(name="awsConfig")
    @Profile( Profiles.AWS_CONFIGURATION_PROFILE )
    public AmazonLaunchConfiguration awsConfig() {
        logger.info( "Using aws configuration: {}", awsConfig );
        if( awsConfig.getRegion().isPresent() ) {
            s3.setRegion( Region.getRegion(awsConfig().getRegion().get() ) );
        }
        return awsConfig;
    }

    @Bean(name="awsConfig")
    @Profile( Profiles.AWS_TESTING_PROFILE )
    public AmazonLaunchConfiguration awsTestingConfig() {
        logger.info( "Using aws configuration: {}", awsTestConfig );
        if( awsTestConfig.getRegion().isPresent() ) {
            s3.setRegion( Region.getRegion(awsConfig().getRegion().get() ) );
        }
        return awsTestConfig;
    }

    @Bean
    public AmazonS3 s3() {
      return s3;
    }

}
