package com.kryptnostic.rhizome.converters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.kryptnostic.rhizome.registries.ObjectMapperRegistry;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.Converter.Factory;

/**
 * A {@link Factory} that handles both json and byte arrays. Based off work by Kai Waldron (kaiwaldron@gmail.com)
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class RhizomeConverter extends Converter.Factory {
    private static final String JSON_MIME_TYPE = "application/json; charset=UTF-8";
    private static final String BYTE_MIME_TYPE = "application/octet-stream";
    private static final Logger logger         = LoggerFactory.getLogger( RhizomeConverter.class );
    private final ObjectMapper  objectMapper;

    public RhizomeConverter() {
        this.objectMapper = ObjectMapperRegistry.getJsonMapper();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter( Type type, Annotation[] annotations, Retrofit retrofit ) {
        return responseBody -> StringUtils
                .equals( MoreObjects.firstNonNull( responseBody.contentType(), "" ).toString(), BYTE_MIME_TYPE )
                        ? responseBody.byteStream()
                        : objectMapper.readValue( responseBody.byteStream(),
                                objectMapper.getTypeFactory().constructType( type ) );
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit ) {
        return obj -> byte[].class.isAssignableFrom( obj.getClass() )
                ? RequestBody.create( okhttp3.MediaType.parse( BYTE_MIME_TYPE ), (byte[]) obj )
                : RequestBody.create( okhttp3.MediaType.parse( JSON_MIME_TYPE ),
                        objectMapper.writeValueAsBytes( obj ) );
    }

    //
    // @Override
    // public Object fromBody( TypedInput body, Type type ) throws ConversionException {
    // try ( InputStream in = body.in() ) {
    // if ( StringUtils.equals( body.mimeType(), BYTE_MIME_TYPE ) ) {
    // return IOUtils.toByteArray( in );
    // }
    // JavaType javaType = objectMapper.getTypeFactory().constructType( type );
    // return objectMapper.readValue( body.in(), javaType );
    // } catch ( IOException e ) {
    // logger.error( "Unable to deserialize object of type {} from body with mime-type {}.",
    // type,
    // body.mimeType(),
    // e );
    // throw new ConversionException( e );
    // }
    // }
    //
    // @Override
    // public TypedOutput toBody( Object object ) {
    // if ( byte[].class.isAssignableFrom( object.getClass() ) ) {
    // return new TypedByteArray( BYTE_MIME_TYPE, (byte[]) object );
    // }
    // try {
    // byte[] json = objectMapper.writeValueAsBytes( object );
    // return new TypedByteArray( JSON_MIME_TYPE, json );// json.getBytes( "UTF-8" )
    // } catch ( JsonProcessingException e ) {
    // throw new AssertionError( e );
    // } /*
    // * catch ( UnsupportedEncodingException e ) { throw new AssertionError( e ); }
    // */
    // }
}