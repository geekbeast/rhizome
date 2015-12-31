package com.kryptnostic.rhizome.serializers;

import java.io.IOException;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.cassandra.CassandraSetProxy;

public abstract class BaseCassandraSetProxyStreamSerializer<K, T, P extends CassandraSetProxy<K, T>>
        implements SelfRegisteringStreamSerializer<P> {

    private final Session                 session;
    private SelfRegisteringValueMapper<T> valueMapper;

    public BaseCassandraSetProxyStreamSerializer( Session session ) {
        this.session = session;
    }

    public BaseCassandraSetProxyStreamSerializer( Session session, SelfRegisteringValueMapper<T> valueMapper ) {
        this.session = session;
        this.valueMapper = valueMapper;
    }

    @Override
    public void write( ObjectDataOutput out, P object ) throws IOException {
        out.writeUTF( object.getKeyspace() );
        out.writeUTF( object.getTable() );
        out.writeUTF( object.getSetId() );
    }

    @Override
    public P read( ObjectDataInput in ) throws IOException {
        String keyspace = in.readUTF();
        String table = in.readUTF();
        String mappedSetId = in.readUTF();

        return newInstance( session, keyspace, table, mappedSetId, valueMapper );
    }

    @Inject
    public void configureValueMapper( SelfRegisteringValueMapper<T> valueMapper ) {
        this.valueMapper = valueMapper;
    }

    protected abstract P newInstance(
            Session session,
            String keyspace,
            String table,
            String mappedSetId,
            SelfRegisteringValueMapper<T> valueMapper );

    @Override
    public void destroy() { /* No-Op */}

    @Override
    public abstract Class<P> getClazz();

}