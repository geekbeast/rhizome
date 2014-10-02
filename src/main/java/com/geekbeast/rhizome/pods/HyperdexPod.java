package com.geekbeast.rhizome.pods;

import org.hyperdex.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;

@Configuration
public class HyperdexPod {
    private static final Logger logger = LoggerFactory.getLogger( HyperdexPod.class );
    
    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();
    
    static {
        HyperdexPreconfigurer.configure();
    }

    @Bean
    public HyperdexConfiguration hyperdexConfiguration() {
        return configuration.getHyperdexConfiguration().get();
    }
    
    @Bean 
    public Client hyperdexClient() {
        Client client = null;

        int port = hyperdexConfiguration().getPort();
        for( String coordinator : hyperdexConfiguration().getCoordinators() ) {
            try {
                client = new Client( coordinator , port );
                if( client != null ) {
                    break;
                }
            } catch( Exception e ) {
                logger.warn( "Unable to connect to coordinator {} on port {}... skipping." , coordinator , port );
            }
        }
        return client;
    }

}
