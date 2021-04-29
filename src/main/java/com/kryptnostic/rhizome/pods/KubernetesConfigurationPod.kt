package com.kryptnostic.rhizome.pods

import com.kryptnostic.rhizome.configuration.ConfigurationConstants
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration
import com.openlattice.ResourceConfigurationLoader
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Used to load configuration from files mounted in the container
 *
 * @author Drew Bailey (drew@openlattice.com)
 */
@Configuration
@Profile(ConfigurationConstants.Profiles.KUBERNETES_CONFIGURATION_PROFILE)
class KubernetesConfigurationPod {
    private val logger = LoggerFactory.getLogger(KubernetesConfigurationPod::class.java)

    @Bean
    fun rhizomeConfiguration(): RhizomeConfiguration {
        return ResourceConfigurationLoader.loadConfigurationFromFile("/etc/openlattice", RhizomeConfiguration::class.java)
    }

    @Bean
    fun jettyConfiguration(): JettyConfiguration {
        return ResourceConfigurationLoader.loadConfigurationFromFile("/etc/openlattice", JettyConfiguration::class.java)
    }
}