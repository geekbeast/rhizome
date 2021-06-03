package com.openlattice.jdbc

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.geekbeast.configuration.postgres.PostgresConfiguration
import com.openlattice.postgres.PostgresTableDefinition
import com.openlattice.postgres.PostgresTableManager
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class DataSourceManager(
        dataSourceConfigurations: Map<String, PostgresConfiguration>,
        healthCheckRegistry: HealthCheckRegistry,
        metricRegistry: MetricRegistry
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataSourceManager::class.java)
        const val DEFAULT_DATASOURCE = "default"
        const val JDBC_URL = "jdbcUrl"
        const val READ_ONLY_URL = "readOnlyUrl"

        fun createHikariConfig(
                hikariProperties: Properties,
                metricRegistry: MetricRegistry,
                healthCheckRegistry: HealthCheckRegistry
        ): HikariConfig {
            val hc = HikariConfig(hikariProperties)

            hc.healthCheckRegistry = healthCheckRegistry
            hc.metricRegistry = metricRegistry

            return hc
        }
    }

    private val dataSources = dataSourceConfigurations.mapValues { (dataSourceName, postgresConfiguration) ->

        val hc = createHikariConfig(postgresConfiguration.hikariConfiguration, metricRegistry, healthCheckRegistry)
        logger.info("JDBC URL for DataSource {} = {}", dataSourceName, hc.jdbcUrl)

        return@mapValues HikariDataSource(hc)
    }

    //Only initialize read only data sources for property configs that specify a readOnlyUrl
    private val readOnlyDataSources = dataSourceConfigurations
            .filterValues { it.hikariConfiguration.containsKey(READ_ONLY_URL) }
            .mapValues { (dataSourceName, postgresConfiguration) ->
                val hikariProperties = Properties()
                hikariProperties.putAll(postgresConfiguration.hikariConfiguration)
                hikariProperties[JDBC_URL] = hikariProperties[READ_ONLY_URL]
                val hc = createHikariConfig(hikariProperties, metricRegistry, healthCheckRegistry)
                logger.info("JDBC URL for DataSource {} = {}", dataSourceName, hc.jdbcUrl)
                return@mapValues HikariDataSource(hc)
            }

    private val tableManagers = dataSources.mapValues { (dataSourceName, dataSource) ->
        val dataSourceConfiguration = dataSourceConfigurations.getValue(dataSourceName)
        PostgresTableManager(
                dataSource,
                dataSourceConfiguration.usingCitus,
                dataSourceConfiguration.initializeIndices,
                dataSourceConfiguration.initializeTables
        )
    }

    fun getDefaultDataSource() = dataSources.getValue(DEFAULT_DATASOURCE)
    fun getDataSource(name: String) = dataSources.getValue(name)
    fun getReadOnlyDataSource( name: String ) = readOnlyDataSources.getValue(name)

    fun registerTables(name: String, vararg tableDefinitions: PostgresTableDefinition) {
        val tm = tableManagers.getValue(name)
        tm.registerTables(*tableDefinitions)
    }

    fun registerTablesWithAllDatasources(vararg tableDefinitions: PostgresTableDefinition) {
        tableManagers.values.forEach { it.registerTables(*tableDefinitions) }
    }
}



