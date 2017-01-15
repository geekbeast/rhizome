package com.kryptnostic.rhizome.cassandra;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;

public interface EmbeddedCassandraManager {
    public void start( String yamlFile ) throws ConfigurationException, TTransportException, IOException;
}
