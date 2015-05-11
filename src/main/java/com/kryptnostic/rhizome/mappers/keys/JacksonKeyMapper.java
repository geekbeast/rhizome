package com.kryptnostic.rhizome.mappers.keys;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class JacksonKeyMapper<K> implements KeyMapper<K> {
    private static final Logger logger = LoggerFactory.getLogger( JacksonKeyMapper.class );

    private final ObjectMapper  mapper;

    public JacksonKeyMapper() {
        this( new ObjectMapper( new SmileFactory() ) );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
    }

    public JacksonKeyMapper( ObjectMapper mapper ) {
        this.mapper = mapper;
    }

    @Override
    public String fromKey( K key ) throws MappingException {
        try {
            return mapper.writeValueAsString( key );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to marshall data" );
            throw new MappingException( "Error marshalling data to map." );
        }
    }

    @Override
    public K toKey( String value ) throws MappingException {
        try {
            return mapper.readValue( value, new TypeReference<K>() {} );
        } catch ( IOException e ) {
            logger.error( "Unable to unmarshall data" );
            throw new MappingException( "Error unmarshalling data to map." );
        }
    }

}
