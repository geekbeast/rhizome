package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.kryptnostic.rhizome.hazelcast.objects.OrderedUUIDSet;
import com.kryptnostic.rhizome.hazelcast.objects.UUIDSet;

public class RhizomeUtils {

    static final Logger logger = LoggerFactory.getLogger( RhizomeUtils.class );

    public static class Serializers {

    }

    public static class Sets {
        public static OrderedUUIDSet newOrderedUUIDSetWithExpectedSize( int expected ) {
            return new OrderedUUIDSet( expectedSize( expected ) );
        }

        public static UUIDSet newUUIDSetWithExpectedSize( int expected ) {
            return new UUIDSet( expectedSize( expected ) );
        }

        public static int expectedSize( int expectedSize ) {
            if ( expectedSize < 0 ) {
                throw new IllegalArgumentException( "expectedSize cannot be negative but was: " + expectedSize );
            }
            if ( expectedSize < 3 ) {
                return expectedSize + 1;
            }
            if ( expectedSize < Ints.MAX_POWER_OF_TWO ) {
                return expectedSize + expectedSize / 3;
            }
            return Integer.MAX_VALUE;
        }
    }

    public static class Streams {

        public static void writeByteArray( ObjectOutput out, byte[] bytes ) throws IOException {
            out.writeInt( bytes.length );
            out.write( bytes, 0, bytes.length );
        }

        public static byte[] readByteArray( ObjectInput in ) throws IOException {
            int size = in.readInt();
            byte[] result = new byte[ size ];
            in.readFully( result );
            return result;
        }

        public static String loadResourceToString( final String path ) {
            String resource = null;
            try ( final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( path ) ) {
                resource = IOUtils.toString( stream );
            } catch ( final IOException | NullPointerException e ) {
                logger.error( "Failed to load resource from " + path, e );
                return null;
            }

            return resource;
        }
    }
}
