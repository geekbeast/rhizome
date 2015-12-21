package com.kryptnostic.rhizome.mapstores.cassandra;

import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SetProxyAwareValueMapper<C, V> implements ValueMapper<C> {

    private final ValueMapper<V> valueMapper;

    public SetProxyAwareValueMapper( ValueMapper<V> valueMapper ) {
        this.valueMapper = valueMapper;
    }

    @Override
    public byte[] toBytes( C value ) throws MappingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public C fromBytes( byte[] data ) throws MappingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

}
