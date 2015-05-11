package com.kryptnostic.rhizome.mapstores;

public class MappingException extends Exception {
    public MappingException( String msg ) {
        super( msg );
    }

    public MappingException( Throwable t ) {
        super( t );
    }
}
