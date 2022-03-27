/**
 * Copyright 2022 Matthew Tamayo-Rios
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

import com.google.common.collect.ImmutableMap
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException

class MailServiceTest : GreenMailTest() {
    companion object {
        private lateinit var mailService: MailService

        const val TEMPLATE_PATH = "mail/templates/test.mustache"
        const val EMAIL_SUBJECT = "TEST E-mail"

        @BeforeClass
        @JvmStatic
        @Throws(Exception::class)
        fun beforeClass() {
            mailService = MailService(testMailServiceConfig)
        }
    }

    @Test
    @Throws(MessagingException::class, InterruptedException::class)
    fun sendEmailTest() {
        val toAddresses =
            listOf("GoJIRA <jira@geekbeast.com>", "Master Chief <mc@geekbeast.com>", "foo@geekbeastgs.com")
        val emailRequest = RenderableEmailRequest(
            Optional.empty(),
            toAddresses,
            templatePath = TEMPLATE_PATH,
            subject = EMAIL_SUBJECT,
            templateObjs = ImmutableMap.of("name",
                                           "Master Chief",
                                           "avatar-path",
                                           "the path",
                                           "registration-url",
                                           "test")
        )

        mailService.sendEmailAfterRendering(emailRequest)

        // waitForIncomingEmail() is useful if sending is done asynchronously in a separate thread

        Assert.assertTrue(greenMailServer.waitForIncomingEmail(DEFAULT_WAIT_TIME.toLong(), 2))
        val emails = greenMailServer.receivedMessages
        Assert.assertEquals(toAddresses.size, emails.size)
        for (i in emails.indices) {
            val email = emails[i]
            Assert.assertEquals(EMAIL_SUBJECT, email.subject)
            Assert.assertEquals(toAddresses.size, email.allRecipients.size)
            Assert.assertNull(email.getRecipients(Message.RecipientType.CC))
            Assert.assertNull(email.getRecipients(Message.RecipientType.BCC))
            Assert.assertEquals(1, email.getHeader("From").size.toLong())
            Assert.assertEquals(mailService.config.defaultFromEmail, email.getHeader("From")[0])
            Assert.assertEquals(1, email.getHeader("To").size.toLong())
            // For efficiency, I update the sendEmail method to spool out all emails through same session.
            // So we cannot guarantee the receiving order.
            // Assert.assertEquals( toAddresses[ i ], email.getHeader( "To" )[ 0 ] );
        }
    }

    @Test(expected = IllegalStateException::class)
    @Throws(IOException::class)
    fun testBadRequest_NullEmailRequest() {
        mailService.sendEmailAfterRendering(
            RenderableEmailRequest(
                Optional.empty(),
                listOf(),
                templateObjs = Any(),
                templatePath = ""
            )
        )
    }

    @Test
    fun testJustinIsBlacklisted() {
        Assert.assertFalse(mailService.isNotBlacklisted("foo@someblacklisteddomain.com"))
    }

    @Test
    fun testOthersAreNotBlacklisted() {
        Assert.assertTrue(mailService.isNotBlacklisted("foo@safe.com"))
    }
}
