package com.kryptnostic.rhizome.mapstores.cassandra;

import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SetProxyAwareValueMapper<C> implements SelfRegisteringValueMapper<C> {

    public SetProxyAwareValueMapper() {
    }

    @Override
    public byte[] toBytes( C value, int bufferSize ) throws MappingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public C fromBytes( byte[] data ) throws MappingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public Class<? extends C> getClazz() {
        return null;
    }

}
