package com.kryptnostic.rhizome.datacenter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ClusterManager {

    private static final Logger logger = LoggerFactory.getLogger( ClusterManager.class );

    public static List<String> getContainerIPsWithTag( String tagKey, Optional<String> tagValue ) {
        return Lists.newArrayList( "localhost" );

    }
}
