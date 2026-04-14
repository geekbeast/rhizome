package com.geekbeast.rhizome.configuration.amazon;

import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Ec2Client ec2 = Ec2Client.builder()
                .region( Region.of( region ) )
                .build();
        Filter tagKey = Filter.builder()
                .name( "tag-key" )
                .values( nodeKey.orElse( null ) )
                .build();
        Filter tagValue = Filter.builder()
                .name( "tag-value" )
                .values( nodeValue.orElse( null ) )
                .build();
        DescribeInstancesRequest req = DescribeInstancesRequest.builder()
                .filters( tagKey, tagValue )
                .build();

        DescribeInstancesResponse describeInstances = ec2.describeInstances( req );

        List<Reservation> reservations = describeInstances.reservations();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        for ( Reservation res : reservations ) {
            for ( Instance instance : res.instances() ) {
                try {
                    if ( instance.state().code() < 17 ) {
                        addresses.add( InetAddress.getByName( instance.privateIpAddress() ) );
                    }
                } catch ( UnknownHostException e ) {
                    logger.error( "Couldn't identify host {}", instance.privateIpAddress(), e );
                }
            }
        }
        return addresses;
    }
}
