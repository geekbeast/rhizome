package com.kryptnostic.rhizome.mappers.values;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SimpleValueMapper<V> implements ValueMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger( SimpleValueMapper.class );

    private final ObjectMapper  mapper;
    private final Class<V>      valueClass;

    public SimpleValueMapper( Class<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
        mapper.registerModule( new JodaModule() );
    }

    public SimpleValueMapper( Class<V> valueClass, ObjectMapper mapper ) {
        this.mapper = mapper;
        this.valueClass = valueClass;
    }

    @Override
    public byte[] toBytes( V value ) throws MappingException {
        try {
            return mapper.writeValueAsBytes( value );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to unmarshall data [{}] to hyperdex.", value, e );
            throw new MappingException( "Error marshalling data to hyperdex." );
        }
    }

    @Override
    public V fromBytes( byte[] data ) throws MappingException {
        try {
            return data == null ? null : mapper.readValue( data, valueClass );
        } catch ( IOException e ) {
            logger.error( "Unable to unmarshall data from hyperdex {}", data, e );
            throw new MappingException( "Error unmarshalling data from hyperdex." );
        }
    }
}
