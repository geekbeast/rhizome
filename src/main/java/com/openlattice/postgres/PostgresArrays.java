/*
 * Copyright (C) 2017. OpenLattice, Inc
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
 */

package com.openlattice.postgres;

import javax.annotation.Nullable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresArrays {
    public static Array createUuidArrayOfArrays( Connection connection, Stream<UUID[]> idArrays ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.UUID.sql(), idArrays.toArray( UUID[][]::new ) );
    }

    public static Array createUuidArray( Connection connection, Stream<UUID> ids ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.UUID.sql(), ids.toArray( UUID[]::new ) );
    }

    /**
     * Creates a Postgres UUID array from a collection of ids. This method is preferred for performance reasons to the
     * stream methods above.
     *
     * @param connection The JDBC connection to the database.
     * @param ids The ids for the array.
     * @return A postgres array of the ids in the iteration order of the ids collection.
     * @throws SQLException If something goes wrong.
     */
    public static Array createUuidArray( Connection connection, Collection<UUID> ids ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.UUID.sql(), ids.toArray( new UUID[ 0 ] ) );
    }

    public static Array createUuidArray( Connection connection, UUID...id ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.UUID.sql(), id );
    }

    public static Array createLongArray( Connection connection, Collection<Long> values ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.BIGINT.sql(), values.toArray( new Long[ 0 ] ) );
    }

    public static Array createIntArray( Connection connection, Integer...value ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.INTEGER.sql(), value );
    }

    public static Array createIntArray( Connection connection, Collection<Integer> values ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.INTEGER.sql(), values.toArray( new Integer[ 0 ] ) );
    }

    public static Array createLongArray( Connection connection, Long...value ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.BIGINT.sql(), value );
    }

    public static Array createTextArray( Connection connection, Stream<String> ids ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.TEXT.sql(), ids.toArray( String[]::new ) );
    }

    public static Array createTextArray( Connection connection, Collection<String> ids ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.TEXT.sql(), ids.toArray( new String[ 0 ] ) );
    }

    public static Array createTextArray( Connection connection, String...text ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.TEXT.sql(), text );
    }

    public static Array createBooleanArray( Connection connection, Collection<Boolean> values ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.BOOLEAN.sql(), values.toArray( new Boolean[ 0 ] ) );
    }

    public static Array createShortArray( Connection connection, Collection<Short> values ) throws SQLException {
        return connection.createArrayOf( PostgresDatatype.SMALLINT.sql(), values.toArray( new Short[ 0 ] ) );
    }

    public static String[] getTextArray( ResultSet rs, String column ) throws SQLException {
        return (String[]) rs.getArray( column ).getArray();
    }

    public static Integer[] getIntArray( ResultSet rs, String column ) throws SQLException {
        return (Integer[]) rs.getArray( column ).getArray();
    }

    public static Long[] getLongArray( ResultSet rs, String column ) throws SQLException {
        return (Long[]) rs.getArray( column ).getArray();
    }

    public static UUID[][] getUuidArrayOfArrays( ResultSet rs, String column ) throws SQLException {
        return (UUID[][]) rs.getArray( column ).getArray();
    }

    public static @Nullable UUID[] getUuidArray( ResultSet rs, String column ) throws SQLException {
        final var arr = rs.getArray( column );
        if ( arr == null ) {
            return null;
        } else {
            return (UUID[]) rs.getArray( column ).getArray();
        }
    }
}
