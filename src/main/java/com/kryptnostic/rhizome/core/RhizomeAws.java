package com.kryptnostic.rhizome.core;

import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.websockets.BaseRhizomeServer;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 * Leaving this in here for now to have an easy way to test startup on AWS.
 */
public class RhizomeAws extends BaseRhizomeServer {
    public static void main( String[] args ) throws Exception {
        new RhizomeAws().start( Profiles.AWS_CONFIGURATION_PROFILE );
    }
}
