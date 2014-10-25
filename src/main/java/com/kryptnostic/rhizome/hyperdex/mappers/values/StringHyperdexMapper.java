package com.kryptnostic.rhizome.hyperdex.mappers.values;

import java.util.Map;

import org.hyperdex.client.ByteString;

import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.mapstores.HyperdexMappingException;

public class StringHyperdexMapper implements HyperdexMapper<String> {
    private static final String DATA_ATTRIBUTE = "data";
    
    @Override
    public Map<String, Object> toHyperdexMap( String value ) throws HyperdexMappingException {
        return ImmutableMap.of(DATA_ATTRIBUTE, value);
    }

    @Override
    public String fromHyperdexMap( Map<String, Object> hyperdexMap ) throws HyperdexMappingException {
        ByteString value = ((ByteString)hyperdexMap.get( DATA_ATTRIBUTE ));
        return value == null ? null : value.toString();
    }

}
