package com.kryptnostic.rhizome.hyperdex.mappers.keys;

import org.hyperdex.client.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;
import com.kryptnostic.rhizome.mapstores.HyperdexMappingException;

public class JacksonHyperdexKeyMapper<K> implements HyperdexKeyMapper<K> {
    private static final Logger logger = LoggerFactory.getLogger( JacksonHyperdexKeyMapper.class );

    private final ObjectMapper  mapper;

    public JacksonHyperdexKeyMapper() {
        this( new ObjectMapper( new SmileFactory() ) );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
    }

    public JacksonHyperdexKeyMapper( ObjectMapper mapper ) {
        this.mapper = mapper;
    }

    @Override
    public ByteString getKey( K key ) throws HyperdexMappingException {
        try {
            return ByteString.wrap( mapper.writeValueAsBytes( key ) );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to marshall data for hyperdex" );
            throw new HyperdexMappingException( "Error marshalling data to hyperdex map." );
        }
    }
}
