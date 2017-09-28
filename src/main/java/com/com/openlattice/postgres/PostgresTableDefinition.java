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

package com.com.openlattice.postgres;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresTableDefinition {
    private String   name;
    private String[] primary;
    private String[] value;
    private boolean  ifNotExists;

    public PostgresTableDefinition primary( String[] primary ) {
        this.primary = primary;
        return this;
    }

    //    public PostgresColumnDefinition value( )
    public String createTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + name
                + "( " + Stream.concat( Arrays.asList( primary ).stream(), Arrays.asList( value ).stream() )
                .collect( Collectors.joining() )
                + ", PRIMARY KEY( " + Arrays.asList( primary ) + ") ) ";
    }
}
