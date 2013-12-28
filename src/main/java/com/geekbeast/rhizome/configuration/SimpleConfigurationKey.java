package com.geekbeast.rhizome.configuration;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class SimpleConfigurationKey implements ConfigurationKey, DataSerializable {
    private static final long serialVersionUID = 8353566116673080764L;
    private String id; 
    
    @JsonCreator
    public SimpleConfigurationKey( String id ) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.id = in.readUTF();
    }
    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF( id );
    }

}
