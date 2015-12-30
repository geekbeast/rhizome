package com.kryptnostic.rhizome.mapstores;

public class MappingException extends Exception {
    private static final long serialVersionUID = -4040494869759537073L;

    public MappingException( String msg ) {
        super( msg );
    }

    public MappingException( Throwable t ) {
        super( t );
    }
}
