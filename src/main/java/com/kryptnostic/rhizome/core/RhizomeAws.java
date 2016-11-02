package com.kryptnostic.rhizome.core;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

public class RhizomeAws {
    public static void main( String[] args ) {
        final AmazonS3 s3 = new AmazonS3Client();
        List<Bucket> buckets = s3.listBuckets();
        System.out.println( "Your Amazon S3 buckets:" );
        for ( Bucket b : buckets ) {
            System.out.println( "* " + b.getName() );
        }
    }
}
