package com.kryptnostic.rhizome.hazelcast.serializers;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.openlattice.rhizome.hazelcast.OrderedUUIDSet;
import com.openlattice.rhizome.hazelcast.UUIDSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class RhizomeUtils {

    static final Logger logger = LoggerFactory.getLogger( RhizomeUtils.class );

    public static class Serializers {

        public static <T extends Enum<T>> void serializeEnumSet( ObjectDataOutput out, Class<T> clazz, Set<T> set )
                throws IOException {
            T[] enumConstants = clazz.getEnumConstants();
            if ( enumConstants == null ) {
                throw new IllegalArgumentException( "This method may only be called for Enum classes" );
            }
            for ( T t : enumConstants ) {
                out.writeBoolean( set.contains( t ) );
            }
        }

        public static <T extends Enum<T>> EnumSet<T> deSerializeEnumSet( ObjectDataInput in, Class<T> clazz )
                throws IOException {
            T[] enumConstants = clazz.getEnumConstants();
            if ( enumConstants == null ) {
                throw new IllegalArgumentException( "This method may only be called for Enum classes" );
            }
            EnumSet<T> elements = EnumSet.<T> noneOf( clazz );
            for ( T t : enumConstants ) {
                if ( in.readBoolean() ) {
                    elements.add( t );
                }
            }
            return elements;
        }

        public static <T> void serializeOptional(
                ObjectDataOutput out,
                Optional<T> object,
                IoPerformingBiConsumer<ObjectDataOutput, T> serializer ) throws IOException {
            out.writeBoolean( object.isPresent() );
            if ( object.isPresent() ) {
                T value = object.get();
                serializer.accept( out, value );
            }
        }

        public static <T> Optional<T> deserializeToOptional(
                ObjectDataInput in,
                IoPerformingFunction<ObjectDataInput, T> deserializer ) throws IOException {
            Optional<T> object = Optional.empty();
            if ( in.readBoolean() ) {
                object = Optional.of( deserializer.apply( in ) );
            }
            return object;
        }

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

        public static void writeStringStringMap( ObjectDataOutput out, Map<String, String> object )
                throws IOException {
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
            try {
                URL resource = Resources.getResource( path );
                return Resources.toString( resource, StandardCharsets.UTF_8 );
            } catch ( IOException | IllegalArgumentException e ) {
                logger.error( "Failed to load resource from " + path, e );
                return null;
            }
        }
    }

    public static class Pods {
        public static Class<?>[] concatenate( Class<?>[]... podSets ) {
            Iterable<Class<?>> concatenatedPods = Iterables.<Class<?>> concat(
                    Iterables.transform( Arrays.<Class<?>[]> asList( podSets ), podSet -> Arrays.asList( podSet ) ) );
            return Iterables.toArray( concatenatedPods, Class.class );
        }
    }
}
