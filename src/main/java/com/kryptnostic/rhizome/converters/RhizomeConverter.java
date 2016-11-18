package com.kryptnostic.rhizome.converters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.registries.ObjectMapperRegistry;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;

/**
 * A {@link Factory} for serialization / deserialization using jackson for retrofit 2.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class RhizomeConverter extends Converter.Factory {
    private static final String JSON_MIME_TYPE      = "application/json";
    private static final String JSON_UTF8_MIME_TYPE = JSON_MIME_TYPE + "; charset=UTF-8";
    private static final Logger logger              = LoggerFactory.getLogger( RhizomeConverter.class );
    private final ObjectMapper  objectMapper;

    public RhizomeConverter() {
        this.objectMapper = ObjectMapperRegistry.getJsonMapper();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter( Type type, Annotation[] annotations, Retrofit retrofit ) {
        if ( ByteConverter.isByteArray( type ) ) {
            return null;
        }

        return responseBody -> {
            MediaType contentType = responseBody.contentType();

            if ( contentType == null ) {
                return null;
            }

            String rawContentType = contentType.toString();
            if ( StringUtils.startsWith( rawContentType, JSON_MIME_TYPE ) ) {
                try {
                    return objectMapper.readValue( responseBody.byteStream(),
                            objectMapper.getTypeFactory().constructType( type ) );
                } catch ( IOException e ) {
                    logger.error( "Unable to read deserialize json response.", e );
                    return null;
                }
            } else if( StringUtils.startsWith( rawContentType, com.google.common.net.MediaType.PLAIN_TEXT_UTF_8.type() )) {
                return IOUtils.toString( responseBody.byteStream() );
            }
            return null;
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit ) {
        if ( ByteConverter.isByteArray( type ) ) {
            return null;
        }
        return obj -> RequestBody.create( MediaType.parse( JSON_UTF8_MIME_TYPE ),
                objectMapper.writeValueAsBytes( obj ) );
    }
}