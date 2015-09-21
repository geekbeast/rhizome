package com.kryptnostic.rhizome.mappers.values;

import java.io.IOException;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.mappers.JacksonValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SimpleValueMapper<V> extends JacksonValueMapper<V> {
    private static final Logger logger = LoggerFactory.getLogger( SimpleValueMapper.class );

    private final Class<V>      valueClass;

    public SimpleValueMapper( Class<V> valueClass ) {
        this( valueClass, new ObjectMapper() );
    }

    public SimpleValueMapper( Class<V> valueClass, ObjectMapper mapper ) {
        super( mapper );

        this.valueClass = valueClass;
    }

    @Override
    public byte[] toBytes( V value ) throws MappingException {
        try {
            return mapper.writeValueAsBytes( value );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to unmarshall class [{}] using data [{}] for mapstore.",
                    valueClass.getCanonicalName(),
                    value,
                    e );
            throw new MappingException( "Error marshalling data." );
        }
    }

    @Override
    public V fromBytes( byte[] data ) throws MappingException {
        try {
            return data == null ? null : mapper.readValue( data, valueClass );
        } catch ( IOException e ) {
            logger.error( "Unable to unmarshall class [{}] using data [{}] from mapstore with string form: {}.",
                    valueClass.getCanonicalName(),
                    data,
                    StringUtils.newStringUtf8( data ),
                    e );
            throw new MappingException( "Error unmarshalling data." );
        }
    }
}
