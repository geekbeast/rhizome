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

import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.openlattice.jdbc.DataSourceManager;
import com.openlattice.jdbc.JdbcPod;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Set;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Configuration
@Profile( PostgresPod.PROFILE )
@Import( JdbcPod.class )
public class PostgresPod {
    public static final String PROFILE = "postgres";

    @Inject RhizomeConfiguration rhizomeConfiguration;

    @Inject
    private HikariDataSource hds;

    @Autowired( required = false )
    private Set<PostgresTables> spt;

    @Inject
    private DataSourceManager dataSourceManager;

    @Bean
    public PostgresTableManager tableManager() throws SQLException {

        /*
         * We first register all tables with the datasource manager and then return the default table manager.
         *
         * Getting the default table manager will cause an IllegalStateException, if postgres configuration
         * was not configured, maintaining compatibility with previous behavior.
         */

        dataSourceManager.registerTables(
                spt
                        .stream()
                        .flatMap( PostgresTables::tables )
                        .toArray( PostgresTableDefinition[]::new )
        );

        return dataSourceManager.getDefaultTableManager();
    }
}