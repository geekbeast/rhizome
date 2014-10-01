package com.kryptnostic.rhizome.mapstores;

import java.io.IOException;
import java.util.Map;

import org.hyperdex.client.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.collect.ImmutableMap;

public class SimpleHyperdexPacker<V> implements HyperdexMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger( SimpleHyperdexPacker.class );
    private static final String DATA_ATTRIBUTE = "data";
    
    private final ObjectMapper mapper;
    private final Class<V> valueClass;
    
    public SimpleHyperdexPacker( Class<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
    }
    
    public SimpleHyperdexPacker( Class<V> valueClass, ObjectMapper mapper ) {
        this.mapper = mapper;
        this.valueClass = valueClass;
    }

    @Override
    public Map<String, Object> toHyperdexMap(V value)  throws HyperdexMappingException {
        try {
            return ImmutableMap.<String,Object>of( DATA_ATTRIBUTE , mapper.writeValueAsString( value ) );
        } catch (JsonProcessingException e) {
            logger.error( "Unable to unmarshall data [{}] to hyperdex map." , value , e );
            throw new HyperdexMappingException( "Error marshalling data to hyperdex map." );
        }
    }

    @Override
    public V fromHyperdexMap(Map<String, Object> hyperdexMap) throws HyperdexMappingException {
        try {
            return mapper.readValue( ((ByteString) hyperdexMap.get( DATA_ATTRIBUTE ) ).toString() , valueClass );
        } catch (IOException e) {
            logger.error( "Unable to unmarshall data from hyperdex map {}" , hyperdexMap , e );
            throw new HyperdexMappingException( "Error unmarshalling data from hyperdex map." );
        }
    }
}
