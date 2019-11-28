package com.kryptnostic.rhizome.datacenter;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DockerInfoService {

    private static final Logger logger = LoggerFactory.getLogger( DockerInfoService.class );

    public static List<String> getContainerIPsWithTag( String tagKey, Optional<String> tagValue ) {
        return Lists.newArrayList("localhost");
    }
}
