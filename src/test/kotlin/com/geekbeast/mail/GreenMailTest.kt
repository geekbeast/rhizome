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
 */
package com.geekbeast.mail

import com.icegreen.greenmail.util.DummySSLSocketFactory
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.Before
import java.security.Security

open class GreenMailTest {
    @Before
    fun startGreenMail() {
    }

    companion object {
        const val DEFAULT_WAIT_TIME = 2000
        const val HOST = "localhost"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        val PORT: Int = ServerSetupTest.SMTPS.port
        @JvmField
        val greenMailServer: GreenMail

        @JvmField
        val testMailServiceConfig: MailServiceConfig

        init {
            // needed to avoid "javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown"
            Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory::class.java.name)
            greenMailServer = GreenMail(ServerSetupTest.SMTPS)
            greenMailServer.setUser(USERNAME, PASSWORD)
            testMailServiceConfig = MailServiceConfig(
                    HOST,
                    PORT,
                    USERNAME,
                    PASSWORD,
                    "tests@geekbeast.com"
            )
            greenMailServer.start()
        }
    }
}