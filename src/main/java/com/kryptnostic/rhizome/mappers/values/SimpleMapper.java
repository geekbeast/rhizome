package com.kryptnostic.rhizome.mappers.values;

import java.io.IOException;
import java.util.Map;

import org.hyperdex.client.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SimpleMapper<V> implements MapStoreDataMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger( SimpleMapper.class );

    private final ObjectMapper  mapper;
    private final Class<V>      valueClass;

    public SimpleMapper( Class<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
        mapper.registerModule( new JodaModule() );
    }

    public SimpleMapper( Class<V> valueClass, ObjectMapper mapper ) {
        this.mapper = mapper;
        this.valueClass = valueClass;
    }

    @Override
    public Map<String, Object> toValueMap( V value ) throws MappingException {
        try {
            return ImmutableMap.<String, Object> of( DATA_ATTRIBUTE, mapper.writeValueAsString( value ) );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to unmarshall data [{}] to hyperdex map.", value, e );
            throw new MappingException( "Error marshalling data to hyperdex map." );
        }
    }

    @Override
    public V fromValueMap( Map<String, Object> hyperdexMap ) throws MappingException {
        try {
            return hyperdexMap == null ? null : mapper.readValue(
                    ( (ByteString) hyperdexMap.get( DATA_ATTRIBUTE ) ).toString(),
                    valueClass );
        } catch ( IOException e ) {
            logger.error( "Unable to unmarshall data from hyperdex map {}", hyperdexMap, e );
            throw new MappingException( "Error unmarshalling data from hyperdex map." );
        }
    }

    @Override
    public V fromString( String str ) throws MappingException {
        try {
            return mapper.readValue( str, valueClass );
        } catch ( IOException e ) {
            logger.error( "Failed to read value", e );
            throw new MappingException( e );
        }
    }

    @Override
    public String toValueString( V value ) throws MappingException {
        try {
            return mapper.writeValueAsString( value );
        } catch ( JsonProcessingException e ) {
            logger.error( "failed to write value string", e );
            throw new MappingException( e );
        }
    }
}
