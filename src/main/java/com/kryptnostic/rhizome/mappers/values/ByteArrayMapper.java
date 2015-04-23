package com.kryptnostic.rhizome.mappers.values;

import java.util.Map;

import org.hyperdex.client.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class ByteArrayMapper implements MapStoreDataMapper<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger( ByteArrayMapper.class );

    @Override
    public Map<String, Object> toValueMap( byte[] value ) {
        return ImmutableMap.of( DATA_ATTRIBUTE, ByteString.wrap( value ) );
    }

    @Override
    public byte[] fromValueMap( Map<String, Object> valueMap ) {
        return ( (ByteString) valueMap.get( DATA_ATTRIBUTE ) ).getBytes();
    }

    @Override
    public byte[] fromString( String str ) throws MappingException {
        try {
            return Base64.decode( str );
        } catch ( Base64DecodingException e ) {
            logger.error( "FromString failed in ByteArrayMapper", e );
            throw new MappingException( e );
        }
    }

    @Override
    public String toValueString( byte[] value ) throws MappingException {
        return Base64.encode( value );
    }
}
