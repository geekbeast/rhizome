package com.kryptnostic.rhizome.cassandra;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public class SimpleCassandraMapper<V> implements CassandraMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger( SimpleCassandraMapper.class );
    private final ObjectMapper  mapper;
    private final Class<V>      valueType;

    public SimpleCassandraMapper( Class<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
        mapper.registerModule( new JodaModule() );
    }

    public SimpleCassandraMapper( Class<V> valueClass, ObjectMapper mapper ) {
        this.mapper = mapper;
        this.valueType = valueClass;
    }

    public V map( String data ) {
        if ( data == null ) {
            return null;
        }
        try {
            return mapper.readValue( data, valueType );
        } catch ( IOException e ) {
            logger.error( "Unable to map value from bytes." );
            return null;
        }
    }

    public String asString( V input ) {
        try {
            return mapper.writeValueAsString( input );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to map value to bytes.", e );
            return null;
        }
    }
}
