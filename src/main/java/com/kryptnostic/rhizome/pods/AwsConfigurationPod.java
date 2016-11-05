package com.kryptnostic.rhizome.pods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 * Used for bootstrapping configuration from a (secure) S3 bucket.
 */
@Profile( Profiles.AWS_CONFIGURATION_PROFILE )
@Configuration
public class AwsConfigurationPod {
    private static final Logger                    logger = LoggerFactory.getLogger( ConfigurationPod.class );
    private static final AmazonS3                  s3     = new AmazonS3Client();
    private static final AmazonLaunchConfiguration awsConfig;
    private static final RhizomeConfiguration      rhizomeConfiguration;
    private static final JettyConfiguration        jettyConfiguration;

    static {
        try {
            awsConfig = ConfigurationService.StaticLoader.loadConfiguration( AmazonLaunchConfiguration.class );
            
            rhizomeConfiguration = ConfigurationService.StaticLoader.loadConfigurationFromS3( s3,
                    awsConfig.getBucket(),
                    awsConfig.getFolder(),
                    RhizomeConfiguration.class );
            jettyConfiguration = ConfigurationService.StaticLoader.loadConfigurationFromS3( s3,
                    awsConfig.getBucket(),
                    awsConfig.getFolder(),
                    JettyConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Error loading configuration!", e );
            throw new Error( "Configuration failure." );
        }
    }

    @Bean
    public RhizomeConfiguration rhizomeConfiguration() {
        return rhizomeConfiguration;
    }

    @Bean
    public JettyConfiguration jettyConfiguration() {
        return jettyConfiguration;
    }

    @Bean
    public AmazonLaunchConfiguration awsConfig() {
        logger.info( "Using aws configuration: {}" , awsConfig );
        return awsConfig;
    }

    @Bean
    public AmazonS3 s3() {
        return s3;
    }

}
