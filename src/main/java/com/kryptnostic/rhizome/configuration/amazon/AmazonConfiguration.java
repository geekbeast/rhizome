package com.kryptnostic.rhizome.configuration.amazon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.base.Optional;

public class AmazonConfiguration {

    public static final String PROVIDER_PROPERTY           = "provider";
    public static final String AWS_REGION_PROPERTY         = "region";
    public static final String AWS_NODE_TAG_KEY_PROPERTY   = "node-tag-key";
    public static final String AWS_NODE_TAG_VALUE_PROPERTY = "node-tag-value";
    public static final String AWS_REGION_DEFAULT          = "us-west-1";

    public static List<InetAddress> getNodesWithTagKeyAndValueInRegion(
            String region,
            Optional<String> nodeKey,
            Optional<String> nodeValue,
            Logger logger ) {
        AmazonEC2Async ec2 = AmazonEC2AsyncClientBuilder.standard()
                .withRegion( region )
                .build();
        Filter tagKey = new Filter()
                .withName( "tag-key" )
                .withValues( nodeKey.orNull() );
        Filter tagValue = new Filter()
                .withName( "tag-value" )
                .withValues( nodeValue.orNull() );
        DescribeInstancesRequest req = new DescribeInstancesRequest().withFilters( tagKey, tagValue );

        DescribeInstancesResult describeInstances = ec2.describeInstances( req );

        List<Reservation> reservations = describeInstances.getReservations();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        for ( Reservation res : reservations ) {
            for ( Instance instance : res.getInstances() ) {
                try {
                    if ( instance.getState().getCode() < 17 ) {
                        addresses.add( InetAddress.getByName( instance.getPrivateIpAddress() ) );
                    }
                } catch ( UnknownHostException e ) {
                    logger.error( "Couldn't identify host {}", instance.getPrivateIpAddress(), e );
                }
            }
        }
        return addresses;
    }
}
