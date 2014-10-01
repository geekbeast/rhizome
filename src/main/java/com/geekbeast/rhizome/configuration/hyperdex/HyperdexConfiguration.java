package com.geekbeast.rhizome.configuration.hyperdex;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class HyperdexConfiguration {
    private static final String HYPERDEX_SPACE_PROPERTY = "space";
    private static final String HYPERDEX_PORT_PROPERTY = "property";
    private static final String HYPERDEX_COORDINATORS_PROPERTY = "coordinators";
    private static final String HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY = "native-bin-path";

    private static final List<String> HYPERDEX_SEED_DEFAULT = ImmutableList.of("127.0.0.1");
    private static final String SPACE_DEFAULT = "default";
    private static final int PORT_DEFAULT = 1982; 
    private static final String NATIVE_BINARY_FOLDER_DEFAULT = "lib/macosx";
    

    private final List<String> coordinators;
    private final String keyspace;
    private final int port;
    private final String nativeBinPath;
    
    @JsonCreator
    public HyperdexConfiguration(  
            @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY ) Optional<List<String>> coordinators ,
            @JsonProperty( HYPERDEX_SPACE_PROPERTY ) Optional<String> keyspace ,
            @JsonProperty( HYPERDEX_PORT_PROPERTY ) Optional<Integer> port ,
            @JsonProperty( HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY ) Optional<String> nativeBinPath ) {
        
        this.coordinators = coordinators.or( HYPERDEX_SEED_DEFAULT );
        this.keyspace = keyspace.or( SPACE_DEFAULT );
        this.port = port.or( PORT_DEFAULT );
        this.nativeBinPath = nativeBinPath.or( NATIVE_BINARY_FOLDER_DEFAULT );
    }
    
    @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY ) 
    public List<String> getCoordinators() {
        return coordinators;
    }
    
    @JsonProperty( HYPERDEX_SPACE_PROPERTY )
    public String getSpace() {
        return keyspace;
    }
    
    @JsonProperty( HYPERDEX_PORT_PROPERTY )
    public int getPort() {
        return port;
    }
    
    @JsonProperty( HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY ) 
    public String getNativeBinPath() {
        return nativeBinPath;
    }

}
