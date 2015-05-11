package com.kryptnostic.rhizome.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public abstract class JacksonValueMapper<V> implements ValueMapper<V> {
    protected final ObjectMapper mapper;

    public JacksonValueMapper() {
        this( new ObjectMapper() );
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new AfterburnerModule() );
        mapper.registerModule( new JodaModule() );
    }

    public JacksonValueMapper( ObjectMapper mapper ) {
        this.mapper = mapper;
    }
}
