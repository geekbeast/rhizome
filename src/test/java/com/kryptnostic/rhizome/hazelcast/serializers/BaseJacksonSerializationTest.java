package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.registries.ObjectMapperRegistry;

public abstract class BaseJacksonSerializationTest<T> {
    protected static final ObjectMapper mapper = ObjectMapperRegistry.getJsonMapper();
    protected static final ObjectMapper smile  = ObjectMapperRegistry.getSmileMapper();

    @Test
    public void testSerdes() throws IOException {
        T data = getSampleData();
        SerializationResult result = serialize( data );
        Assert.assertEquals( data, deserializeJsonString( result ) );
        Assert.assertEquals( data, deserializeJsonBytes( result ) );
        Assert.assertEquals( data, deserializeSmileBytes( result ) );
    }

    protected SerializationResult serialize( T data ) throws IOException {
        return new SerializationResult()
                .setJsonString( mapper.writeValueAsString( data ) )
                .setJsonBytes( mapper.writeValueAsBytes( data ) )
                .setSmileBytes( smile.writeValueAsBytes( data ) );
    }

    protected T deserializeJsonString( SerializationResult result ) throws IOException {
        return mapper.readValue( result.getJsonString(), getClazz() );
    }

    protected T deserializeJsonBytes( SerializationResult result ) throws IOException {
        return mapper.readValue( result.getJsonBytes(), getClazz() );
    }

    protected T deserializeSmileBytes( SerializationResult result ) throws IOException {
        return smile.readValue( result.getSmileBytes(), getClazz() );
    }

    protected static void registerModule( Consumer<ObjectMapper> c ) {
        c.accept( mapper );
        c.accept( smile );
    }
    
    protected static class SerializationResult {
        private String jsonString;
        private byte[] jsonBytes;
        private byte[] smileBytes;

        public SerializationResult() {}

        public String getJsonString() {
            return jsonString;
        }

        public byte[] getJsonBytes() {
            return jsonBytes;
        }

        public byte[] getSmileBytes() {
            return smileBytes;
        }

        public SerializationResult setJsonString( String jsonString ) {
            this.jsonString = jsonString;
            return this;
        }

        public SerializationResult setJsonBytes( byte[] jsonBytes ) {
            this.jsonBytes = jsonBytes;
            return this;
        }

        public SerializationResult setSmileBytes( byte[] smileBytes ) {
            this.smileBytes = smileBytes;
            return this;
        }
    }

    protected abstract T getSampleData();

    protected abstract Class<T> getClazz();
}
