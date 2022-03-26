/**
 * Copyright 2022 Matthew Tamayo-Rios (matthew@geekbeast.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geekbeast.mail

import com.fasterxml.jackson.annotation.JsonIgnore
import com.geekbeast.rhizome.configuration.Configuration
import com.geekbeast.rhizome.configuration.ConfigurationKey
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey
import com.geekbeast.rhizome.configuration.configuration.annotation.ReloadableConfiguration
import com.google.common.collect.Sets
import jdk.jfr.Enabled

@ReloadableConfiguration(uri = "mail.yaml")
data class MailServiceConfig(
        val smtpHost: String,
        val smtpPort: Int,
        val username: String,
        val password: String,
        val defaultFromEmail: String,
        val domainBlacklist: Set<String> = Sets.newHashSet("someblacklisteddomain.com"),
        val enabled: Boolean = false
) : Configuration {

    @JsonIgnore
    override fun getKey(): ConfigurationKey {
        return Companion.key
    }

    companion object {
        private const val serialVersionUID = -6047689414585379842L
        @JvmField
        val key: ConfigurationKey = SimpleConfigurationKey("mail.yaml")

        @JvmStatic
        fun key(): ConfigurationKey {
            return key
        }
    }
}