package com.kryptnostic.rhizome.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public class SimpleConfigurationKey implements ConfigurationKey, DataSerializable {
    private String uri;

    public SimpleConfigurationKey() {
        uri = null;
    }

    @JsonCreator
    public SimpleConfigurationKey( String uri ) {
        initialize( uri );
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void readData( ObjectDataInput in ) throws IOException {
        this.uri = in.readString();
    }

    @Override
    public void writeData( ObjectDataOutput out ) throws IOException {
        out.writeUTF( uri );
    }

    public void initialize( String uri ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( uri ), "Configuration key uri cannot be blank." );
        Preconditions.checkState( this.uri == null, "Configuration key has already been initialized." );
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "SimpleConfigurationKey [uri=" + uri + "]";
    }

    public static ConfigurationKey fromUri( String uri ) {
        return new SimpleConfigurationKey( uri );
    }
}
