package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
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

        public static void writeStringStringMap( ObjectDataOutput out, Map<String, String> object ) throws IOException {
            int size = object.size();
            Set<String> keys = object.keySet();
            Collection<String> vals = object.values();
            RhizomeUtils.Streams.writeStringArray( out, keys.toArray( new String[ size ] ) );
            RhizomeUtils.Streams.writeStringArray( out, vals.toArray( new String[ size ] ) );
        }

        public static Map<String, String> readStringStringMap( ObjectDataInput in ) throws IOException {
            String[] keys = RhizomeUtils.Streams.readStringArray( in );
            String[] vals = RhizomeUtils.Streams.readStringArray( in );
            Map<String, String> map = Maps.newHashMapWithExpectedSize( keys.length );
            for ( int i = 0; i < keys.length; i++ ) {
                map.put( keys[ i ], vals[ i ] );
            }
            return map;
        }

        public static void writeStringArray( ObjectDataOutput out, String[] strings ) throws IOException {
            out.writeInt( strings.length );
            for ( String string : strings ) {
                out.writeUTF( string );
            }
        }

        public static String[] readStringArray( ObjectDataInput in ) throws IOException {
            int size = in.readInt();
            String[] strings = new String[ size ];

            for ( int i = 0; i < size; i++ ) {
                strings[ i ] = in.readUTF();
            }
            return strings;
        }

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
