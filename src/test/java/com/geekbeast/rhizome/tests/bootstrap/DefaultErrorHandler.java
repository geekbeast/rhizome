package com.geekbeast.rhizome.tests.bootstrap;

import javax.activity.InvalidActivityException;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.springframework.security.access.AccessDeniedException;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError( RetrofitError cause ) {
        Response r = cause.getResponse();
        if ( r != null && r.getStatus() == 401 ) {
            return new NotAuthorizedException( cause );
        }
        if ( r != null && r.getStatus() == 403 ) {
            return new AccessDeniedException( r.getReason(), cause );
        }
        if ( r != null && r.getStatus() == 404 ) {
            return new NotFoundException( cause );
        }
        if ( r != null && r.getStatus() == 400 ) {
            String msg = parseMsg( cause );
            if ( msg != null ) {
                return new InvalidActivityException( msg );
            }
            return new InvalidActivityException( cause );
        }
        if ( r != null && r.getStatus() == 500 ) {
            String msg = parseMsg( cause );
            if ( msg != null ) {
                return RetrofitError.unexpectedError( cause.getUrl(), new InvalidActivityException( msg ) );
            }
        }

        return cause;
    }

    private String parseMsg( RetrofitError cause ) {
        return cause.getResponse().getReason();
    }
}
