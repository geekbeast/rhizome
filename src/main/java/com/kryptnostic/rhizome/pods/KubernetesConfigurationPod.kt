package com.kryptnostic.rhizome.pods

import com.kryptnostic.rhizome.configuration.ConfigurationConstants
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import javax.inject.Inject

/**
 * Used to load configuration from files mounted in the container
 *
 * @author Drew Bailey (drew@openlattice.com)
 */
@Configuration
@Import(ConfigurationLoaderPod::class)
@Profile(ConfigurationConstants.Profiles.KUBERNETES_CONFIGURATION_PROFILE)
class KubernetesConfigurationPod {

    @Inject
    private lateinit var configurationLoader: ConfigurationLoader

    @Bean
    fun rhizomeConfiguration(): RhizomeConfiguration {
        return configurationLoader.logAndLoad("rhizome", RhizomeConfiguration::class.java)
    }

    @Bean
    fun jettyConfiguration(): JettyConfiguration {
        return configurationLoader.logAndLoad("jetty", JettyConfiguration::class.java)
    }
}