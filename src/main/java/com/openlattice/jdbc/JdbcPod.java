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

package com.openlattice.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.MetricsPod;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Configuration
@Import( { ConfigurationPod.class, MetricsPod.class } )
public class JdbcPod {
    private static final Logger logger = LoggerFactory.getLogger( JdbcPod.class );
    @Inject
    private RhizomeConfiguration rhizomeConfiguration;

    @Inject
    private HealthCheckRegistry healthCheckRegistry;

    @Inject
    private MetricRegistry metricRegistry;

    @Bean
    public HikariDataSource hikariDataSource() {
        if ( rhizomeConfiguration.getHikariConfiguration().isPresent() ) {
            HikariConfig hc = new HikariConfig( rhizomeConfiguration.getHikariConfiguration().get() );
            logger.info( "JDBC URL = {}", hc.getJdbcUrl() );
            HikariDataSource hds = new HikariDataSource( hc );
            hds.setHealthCheckRegistry( healthCheckRegistry );
            hds.setMetricRegistry( metricRegistry );
            return hds;
        } else {
            return null;
        }
    }

}
