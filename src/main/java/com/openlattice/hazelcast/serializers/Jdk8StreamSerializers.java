/*
 * Copyright (C) 2018. OpenLattice, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 *
 */

package com.openlattice.hazelcast.serializers;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class Jdk8StreamSerializers {
    public static abstract class AbstractOffsetDateTimeStreamSerializer
            implements SelfRegisteringStreamSerializer<OffsetDateTime> {
        @Override public Class<OffsetDateTime> getClazz() {
            return OffsetDateTime.class;
        }

        @Override public void write( ObjectDataOutput out, OffsetDateTime object ) throws IOException {
            serialize( out, object );
        }

        @Override public OffsetDateTime read( ObjectDataInput in ) throws IOException {
            return deserialize( in );
        }

        @Override public void destroy() {

        }

        public static OffsetDateTime deserialize( ObjectDataInput in ) throws IOException {
            ZoneOffset offset = AbstractZoneOffsetStreamSerializer.deserialize( in );
            LocalDateTime ldt = AbstractLocalDateTimeStreamSerializer.deserialize( in );
            return OffsetDateTime.of( ldt, offset );
        }

        public static void serialize( ObjectDataOutput out, OffsetDateTime object ) throws IOException {
            AbstractZoneOffsetStreamSerializer.serialize( out, object.getOffset() );
            AbstractLocalDateTimeStreamSerializer.serialize( out, object.toLocalDateTime() );
        }
    }

    public static abstract class AbstractLocalDateTimeStreamSerializer
            implements SelfRegisteringStreamSerializer<LocalDateTime> {

        @Override public Class<LocalDateTime> getClazz() {
            return LocalDateTime.class;
        }

        @Override public void write( ObjectDataOutput out, LocalDateTime object ) throws IOException {
            serialize( out, object );
        }

        @Override public LocalDateTime read( ObjectDataInput in ) throws IOException {
            return deserialize( in );
        }

        @Override public void destroy() {

        }

        public static LocalDateTime deserialize( ObjectDataInput in ) throws IOException {
            LocalDate ld = AbstractLocalDateStreamSerializer.deserialize( in );
            LocalTime lt = AbstractLocalTimeStreamSerializer.deserialize( in );
            return LocalDateTime.of( ld, lt );
        }

        public static void serialize( ObjectDataOutput out, LocalDateTime localDateTime ) throws IOException {
            AbstractLocalDateStreamSerializer.serialize( out, localDateTime.toLocalDate() );
            AbstractLocalTimeStreamSerializer.serialize( out, localDateTime.toLocalTime() );
        }
    }

    public static abstract class AbstractLocalDateStreamSerializer
            implements SelfRegisteringStreamSerializer<LocalDate> {

        @Override public Class<LocalDate> getClazz() {
            return LocalDate.class;
        }

        @Override public void write( ObjectDataOutput out, LocalDate object ) throws IOException {
            serialize( out, object );
        }

        @Override public LocalDate read( ObjectDataInput in ) throws IOException {
            return deserialize( in );
        }

        @Override public void destroy() {

        }

        public static LocalDate deserialize( ObjectDataInput in ) throws IOException {
            final int year = in.readInt();
            final int month = in.readInt();
            final short dayOfMonth = in.readShort();
            return LocalDate.of( year, month, dayOfMonth );
        }

        public static void serialize( ObjectDataOutput out, LocalDate localDate ) throws IOException {
            out.writeInt( localDate.getYear() );
            out.writeInt( localDate.getMonthValue() );
            out.writeShort( localDate.getDayOfMonth() );
        }
    }

    public static class AbstractLocalTimeStreamSerializer implements SelfRegisteringStreamSerializer<LocalTime> {
        @Override public Class<LocalTime> getClazz() {
            return LocalTime.class;
        }

        @Override public void write( ObjectDataOutput out, LocalTime object ) throws IOException {

        }

        @Override public LocalTime read( ObjectDataInput in ) throws IOException {
            return null;
        }

        @Override public int getTypeId() {
            return 0;
        }

        @Override public void destroy() {

        }

        public static LocalTime deserialize( ObjectDataInput in ) throws IOException {
            int[] parts = in.readIntArray();
            return LocalTime.of( parts[ 0 ], parts[ 1 ], parts[ 2 ], parts[ 3 ] );
        }

        public static void serialize( ObjectDataOutput out, LocalTime localTime ) throws IOException {
            out.writeIntArray( new int[] { localTime.getHour(), localTime.getMinute(), localTime.getSecond(),
                    localTime.getNano() } );
        }
    }

    public static abstract class AbstractZoneOffsetStreamSerializer
            implements SelfRegisteringStreamSerializer<ZoneOffset> {
        @Override public Class<ZoneOffset> getClazz() {
            return ZoneOffset.class;
        }

        @Override public void write( ObjectDataOutput out, ZoneOffset object ) throws IOException {
            serialize( out, object );
        }

        @Override public ZoneOffset read( ObjectDataInput in ) throws IOException {
            return deserialize( in );
        }

        @Override public void destroy() {

        }

        public static ZoneOffset deserialize( ObjectDataInput in ) throws IOException {
            return ZoneOffset.of( in.readUTF() );
        }

        public static void serialize( ObjectDataOutput out, ZoneOffset offset ) throws IOException {
            out.writeUTF( offset.getId() );
        }
    }
}
