package com.geekbeast.rhizome.registries;

import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.collect.Maps;

public final class ObjectMapperRegistry {

    private static final ConcurrentMap<String, ObjectMapper> mappers = Maps.newConcurrentMap();

    private static final String YAML_MAPPER  = "yaml-mapper";
    private static final String SMILE_MAPPER = "smile-mapper";
    private static final String JSON_MAPPER = "json-mapper";

    static {
        mappers.put( YAML_MAPPER, createYamlMapper() );
        mappers.put( SMILE_MAPPER, createSmileMapper() );
        mappers.put(JSON_MAPPER, createJsonMapper() );
    }

    private ObjectMapperRegistry() {}

    public static ObjectMapper register( String name, ObjectMapper mapper ) {
        return mappers.putIfAbsent( name, mapper );
    }

    public static ObjectMapper getMapper( String name ) {
        return mappers.get( name );
    }

    protected static ObjectMapper createYamlMapper() {
        ObjectMapper yamlMapper = new ObjectMapper( new YAMLFactory() );
        yamlMapper.registerModule( new GuavaModule() );
        yamlMapper.registerModule( new AfterburnerModule() );
        yamlMapper.registerModule( new JodaModule() );
        return yamlMapper;
    }

    protected static ObjectMapper createSmileMapper() {
        ObjectMapper smileMapper = new ObjectMapper( new SmileFactory() );
        smileMapper.registerModule( new GuavaModule() );
        smileMapper.registerModule( new AfterburnerModule() );
        smileMapper.registerModule( new JodaModule() );
        return smileMapper;
    }

    protected static ObjectMapper createJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule( new GuavaModule() );
        mapper.registerModule( new JodaModule() );
        mapper.registerModule( new AfterburnerModule() );
        mapper.registerModule( new JodaModule() );
        return mapper;
    }

    public static ObjectMapper getYamlMapper() {
        return ObjectMapperRegistry.getMapper( ObjectMapperRegistry.YAML_MAPPER );
    }

    public static ObjectMapper getSmileMapper() {
        return ObjectMapperRegistry.getMapper( ObjectMapperRegistry.SMILE_MAPPER );
    }

    public static ObjectMapper getJsonMapper() {
        return ObjectMapperRegistry.getMapper( ObjectMapperRegistry.JSON_MAPPER);
    }

}
