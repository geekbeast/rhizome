package com.kryptnostic.rhizome.hyperdex.mappers.values;

import java.util.Map;

import org.hyperdex.client.ByteString;

import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.mapstores.HyperdexMappingException;

public class ByteArrayHyperdexMapper implements HyperdexMapper<byte[]> {
    private static final String DATA_ATTRIBUTE = "data";
    
    @Override
    public Map<String, Object> toHyperdexMap( byte[] value ) throws HyperdexMappingException {
        return ImmutableMap.of(DATA_ATTRIBUTE, ByteString.wrap( value ) );
    }

    @Override
    public byte[] fromHyperdexMap( Map<String, Object> hyperdexMap ) throws HyperdexMappingException {
        return ((ByteString)hyperdexMap.get( DATA_ATTRIBUTE )).getBytes();
    }
}
