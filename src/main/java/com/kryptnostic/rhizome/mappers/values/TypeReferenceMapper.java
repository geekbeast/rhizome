package com.kryptnostic.rhizome.mappers.values;

import java.io.IOException;
import java.util.Map;

import org.hyperdex.client.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class TypeReferenceMapper<V> implements MapStoreDataMapper<V> {
    private static final Logger    logger = LoggerFactory.getLogger( TypeReferenceMapper.class );

    private final ObjectMapper     mapper;
    private final TypeReference<V> reference;

    public TypeReferenceMapper( TypeReference<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
    }

    public TypeReferenceMapper( TypeReference<V> valueClass, ObjectMapper mapper ) {
        this.mapper = mapper;
        this.reference = valueClass;
    }

    @Override
    public Map<String, Object> toValueMap( V value ) throws MappingException {
        try {
            return ImmutableMap.<String, Object> of(
                    DATA_ATTRIBUTE,
                    ByteString.wrap( mapper.writeValueAsBytes( value ) ) );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to marshall data [{}] to map.", value, e );
            throw new MappingException( "Error marshalling data to map." );
        }
    }

    @Override
    public V fromValueMap( Map<String, Object> hyperdexMap ) throws MappingException {
        try {
            return hyperdexMap == null ? null : mapper.readValue(
                    ( (ByteString) hyperdexMap.get( DATA_ATTRIBUTE ) ).getBytes(),
                    reference );
        } catch ( IOException e ) {
            logger.error( "Unable to unmarshall data from map {}", hyperdexMap, e );
            throw new MappingException( "Error unmarshalling data from map." );
        }
    }

    @Override
    public V fromString( String str ) throws MappingException {
        try {
            return mapper.readValue( str, reference );
        } catch ( IOException e ) {
            logger.error( "Failed to read value", e );
            throw new MappingException( e );
        }
    }

    @Override
    public String toValueString( V value ) throws MappingException {
        try {
            String str = mapper.writeValueAsString( value );
            return str;
        } catch ( JsonProcessingException e ) {
            logger.error( "failed to write value string {}", value, e );
            throw new MappingException( e );
        }
    }

}
