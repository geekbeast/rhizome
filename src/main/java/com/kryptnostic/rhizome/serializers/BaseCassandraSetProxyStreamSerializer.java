package com.kryptnostic.rhizome.serializers;

import java.io.IOException;

import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.cassandra.CassandraSetProxy;

public abstract class BaseCassandraSetProxyStreamSerializer<K, T, P extends CassandraSetProxy<K, T>>
        implements SelfRegisteringStreamSerializer<P> {

    private final Session                       session;
    private final SelfRegisteringValueMapper<T> valueType;

    public BaseCassandraSetProxyStreamSerializer( Session session, SelfRegisteringValueMapper<T> valueType ) {
        this.session = session;
        this.valueType = valueType;
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

        return newInstance( session, keyspace, table, mappedSetId, valueType );
    }

    protected abstract P newInstance(
            Session session,
            String keyspace,
            String table,
            String mappedSetId,
            SelfRegisteringValueMapper<T> forName );

    @Override
    public void destroy() { /* No-Op */}

    @Override
    public abstract Class<P> getClazz();

}