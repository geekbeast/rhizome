package com.kryptnostic.rhizome.core;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.websockets.BaseRhizomeServer;

public class RhizomeAws extends BaseRhizomeServer {
    public static void main( String[] args ) throws Exception {
        new RhizomeAws().start( Profiles.AWS_CONFIGURATION_PROFILE );
        final AmazonS3 s3 = new AmazonS3Client();
        ObjectListing bucket = s3.listObjects( "loom-prod-config" );
        S3Object obj = s3.getObject( "loom-prod-config", "auth0.yaml" );
        List<S3ObjectSummary> objects = bucket.getObjectSummaries();
        for( S3ObjectSummary summary : objects ) {
            System.out.println( "Key="+summary.getKey() + ", size=" + summary.getSize() );
        }
    }
}
