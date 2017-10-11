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

//@formatter:off
import java.util.EnumSet; /**
 * From https://www.postgresql.org/docs/9.5/static/datatype.html
 * <table summary ="Postgres datatypes from docs">
 *     <tr>
 *     <th>Name</th>
 *     <th>Storage Size</th>
 *     <th>Description</th>
 *     <th>Range</th>
 *     </tr>
 *     <tr><td>smallint</td><td>2 bytes</td><td>small-range integer</td><td>-32768 to +32767</td></tr>
 *     <tr><td>integer</td><td>4 bytes</td><td>typical choice for integer</td><td>-2147483648 to +2147483647</tr>
 *     <tr><td>bigint</td><td>8 bytes</td><td>large-range integer</td><td>-9223372036854775808 to +9223372036854775807</td></tr>
 *     <tr><td>decimal</td><td>variable</td><td>user-specified precision, exact</td><td>up to 131072 digits before the decimal point; up to 16383 digits after the decimal point</td></tr>
 *     <tr><td>numeric</td><td>variable</td><td>user-specified precision, exact</td><td>up to 131072 digits before the decimal point; up to 16383 digits after the decimal point</td></tr>
 *     <tr><td>real</td><td>4 bytes</td><td>variable-precision, inexact</td><td>6 decimal digits precision</td></tr>
 *     <tr><td>double</td><td>precision</td><td>8 bytes	variable-precision, inexact</td><td>15 decimal digits precision</td></tr>
 *     <tr><td>smallserial</td><td>2 bytes</td><td>small autoincrementing integer</td><td>1 to 32767</td></tr>
 *     <tr><td>serial</td><td>4 bytes</td><td>autoincrementing integer</td><td>1 to 2147483647</td></tr>
 *     <tr><td>bigserial</td><td>8 bytes</td><td>large autoincrementing integer</td><td>1 to 9223372036854775807</td></tr>
 *     <tr><td>bytea</td><td>1 or 4 bytes plus the actual binary string</td><td>variable-length binary string</td><td></td></tr>
 *     <tr><td>boolean</td><td>1 byte</td><td>state of true or false</td><td></td></tr>
 * </table>
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 *
 */
//@formatter:on
public enum PostgresDatatype {
    SMALLINT, SMALLINT_ARRAY,
    INTEGER, INTEGER_ARRAY,
    BIGINT, BIGINT_ARRAY,
    DECIMAL,
    NUMERIC,
    DOUBLE, DOUBLE_ARRAY,
    SERIAL,
    BIGSERIAL,
    BYTEA,
    BOOLEAN,
    TIMESTAMPTZ, TIMESTAMPTZ_ARRAY,
    UUID, UUID_ARRAY,
    TEXT, TEXT_ARRAY;

    private static final EnumSet<PostgresDatatype> ARRAY_TYPES = EnumSet
            .of( SMALLINT_ARRAY, INTEGER_ARRAY, BIGINT_ARRAY, DOUBLE_ARRAY, TIMESTAMPTZ_ARRAY, UUID_ARRAY, TEXT_ARRAY );

    public String sql() {
        return ARRAY_TYPES.contains( this ) ? name().replace( "_ARRAY", "[]" ) : name();
    }
}
