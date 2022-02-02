/*
 * Copyright (C) 2019. OpenLattice, Inc.
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

package com.geekbeast.configuration.postgres

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

private const val HIKARI_CONFIGURATION_PROPERTY = "hikari"
private const val USING_CITUS_PROPERTY = "citus"
private const val INITIALIZE_INDICES_PROPERTY = "initialize-indices"
private const val INITIALIZE_TABLES = "initialize-tables"
private const val FLAVOR_PROPERTY = "flavor"

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class PostgresConfiguration(
        @JsonProperty(HIKARI_CONFIGURATION_PROPERTY) val hikariConfiguration: Properties,
        @JsonProperty(USING_CITUS_PROPERTY) val usingCitus: Boolean = false,
        @JsonProperty(
                FLAVOR_PROPERTY
        ) val flavor: PostgresFlavor = if (usingCitus) PostgresFlavor.CITUS else PostgresFlavor.VANILLA,
        @JsonProperty(INITIALIZE_INDICES_PROPERTY) val initializeIndices: Boolean = true,
        @JsonProperty(INITIALIZE_TABLES) val initializeTables: Boolean = true
)

enum class PostgresFlavor {
    REDSHIFT,
    CITUS,
    VANILLA,
    ANY
}