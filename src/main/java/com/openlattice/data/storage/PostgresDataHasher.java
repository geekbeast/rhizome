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

package com.openlattice.data.storage;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.util.UUID;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public final class PostgresDataHasher {
    private static final HashFunction hf = Hashing.murmur3_128();

    private PostgresDataHasher() {
    }

    public static byte[] hashObject( Object value, EdmPrimitiveTypeKind dataType ) {
        final Hasher h = hf.newHasher();
        switch ( dataType ) {
            case Boolean:
                h.putBoolean( (Boolean) value );
                break;
            case Binary:
                h.putBytes( (byte[]) value );
                break;
            case Date:
            case DateTimeOffset:
            case Duration:
            case TimeOfDay:
                h.putString( value.toString(), Charsets.UTF_8 );
                break;
            case Guid:
                final UUID id = (UUID) value;
                h.putLong( id.getLeastSignificantBits() );
                h.putLong( id.getMostSignificantBits() );
                break;

            case Decimal:
            case Double:
            case Single:
                h.putDouble( (Double) value );
                break;

            case Byte:
            case SByte:
                h.putByte( (Byte) value );
                break;
            case Int16:
                h.putShort( (Short) value );
                break;
            case Int32:
                h.putInt( (Integer) value );
                break;
            case Int64:
                h.putLong( (Long) value );
                break;
            case String:
            case GeographyPoint:
                h.putString( (String) value, Charsets.UTF_8 );
                break;
            default:
                throw new UnsupportedOperationException( "Unable to hash datatype: " + dataType.toString() );
        }
        return h.hash().asBytes();
    }

    public static String hashObjectToHex( Object value, EdmPrimitiveTypeKind dataType ) {
        final Hasher h = hf.newHasher();
        switch ( dataType ) {
            case Boolean:
                h.putBoolean( (Boolean) value );
                break;
            case Binary:
                h.putBytes( (byte[]) value );
                break;
            case Date:
            case DateTimeOffset:
            case Duration:
            case TimeOfDay:
                h.putString( value.toString(), Charsets.UTF_8 );
                break;
            case Guid:
                final UUID id = (UUID) value;
                h.putLong( id.getLeastSignificantBits() );
                h.putLong( id.getMostSignificantBits() );
                break;

            case Decimal:
            case Double:
            case Single:
                h.putDouble( (Double) value );
                break;

            case Byte:
            case SByte:
                h.putByte( (Byte) value );
                break;
            case Int16:
                h.putShort( (Short) value );
                break;
            case Int32:
                h.putInt( (Integer) value );
                break;
            case Int64:
                h.putLong( (Long) value );
                break;
            case String:
            case GeographyPoint:
                h.putString( (String) value, Charsets.UTF_8 );
                break;
            default:
                throw new UnsupportedOperationException( "Unable to hash datatype: " + dataType.toString() );
        }
        return h.hash().toString();
    }
}


