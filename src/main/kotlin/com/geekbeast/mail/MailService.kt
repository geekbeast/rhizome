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

import com.google.common.base.Preconditions
import jodd.mail.*
import org.slf4j.LoggerFactory
import java.io.IOException

class MailService(val config: MailServiceConfig) {
    private val logger = LoggerFactory.getLogger(MailService::class.java)
    private val smtpServer: SmtpServer

    init {
        Preconditions.checkNotNull<Any>(config, "Mail Service configuration cannot be null.")
        smtpServer = SmtpSslServer
            .create()
            .host(config.smtpHost)
            .port(config.smtpPort)
            .auth(config.username, config.password)
            .ssl(true)
            .buildSmtpMailServer()
        // TODO: "javax.net.ssl.SSLException: Unrecognized SSL message, plaintext connection?"
        // TODO: figure out why we get above exception when plaintextOverTLS is false and port is 587 (works for 465)
        // .plaintextOverTLS(false)
        // .startTlsRequired(true);
        logger.info("Mail Service successfully configured and initialized!")
    }

    fun sendEmails(emailRequests: List<EmailRequest>) {
        if (config.enabled) {
            val session = smtpServer.createSession()
            session.open()
            session.use { s ->
                emailRequests.forEach { emailRequest ->
                    s.sendMail(renderEmail(emailRequest))
                }
            }
        } else {
            logger.info("Mail service disabled, ignoring $emailRequests")
        }
    }

    fun sendEmailAfterRendering(emailRequest: RenderableEmailRequest) {
        sendEmails(listOf(emailRequest))
    }

    private fun renderPlaintextEmail(emailRequest: EmailRequest): Email {
        check(emailRequest !is RenderableEmailRequest) { "Only plaintext e-mail is supported by this API." }
        val toAddresses = getToAddresses(emailRequest)
        val email = Email.create()
            .from(emailRequest.from.orElse(config.defaultFromEmail))
            .subject(emailRequest.subject)
            .to(*toAddresses.toTypedArray())
        return if (emailRequest.html) {
            email.textMessage(emailRequest.body)
        } else {
            email.textMessage(emailRequest.body)
        }
    }

    private fun renderEmail(emailRequest: EmailRequest): Email {
        return when (emailRequest) {
            is RenderableEmailRequest -> renderEmailTemplate(emailRequest)
            else -> renderPlaintextEmail(emailRequest)
        }
    }

    private fun getToAddresses(emailRequest: EmailRequest): List<String> {
        val toAddresses = emailRequest.to.filter { isNotBlacklisted(it) }
        logger.info("filtered e-mail addresses that are blacklisted.")
        check(toAddresses.isNotEmpty()) { "Must include at least one valid e-mail address." }
        return toAddresses
    }

    private fun renderEmailTemplate(emailRequest: RenderableEmailRequest): Email {
        val toAddresses = getToAddresses(emailRequest)

        val template: String = try {
            TemplateUtils.loadTemplate(emailRequest.templatePath)
        } catch (e: IOException) {
            throw InvalidTemplateException(
                "Invalid Email Template: " + emailRequest.templatePath,
                e
            )
        }

        val templateHtml: String = TemplateUtils.DEFAULT_TEMPLATE_COMPILER
            .compile(template)
            .execute(emailRequest.templateObjs ?: Any())

        val email: Email = Email.create()
            .from(emailRequest.from.orElse(config.defaultFromEmail))
            .subject(emailRequest.subject)
            .htmlMessage(templateHtml)
            .to(*toAddresses.toTypedArray())

        if (emailRequest.byteArrayAttachment.isPresent) {
            val attachments: Array<EmailAttachment<*>> = emailRequest.byteArrayAttachment.get()
            for (attachment in attachments) {
                email.attachment(attachment)
            }

        }
        if (emailRequest.attachmentPaths.isPresent) {
            val paths: Array<String> = emailRequest.attachmentPaths.get()
            for (path in paths) {
                email.attachment(EmailAttachment.with().content(path))
            }
        }

        return email
    }

    fun isNotBlacklisted(to: String): Boolean {
        return try {
            val parsedAddress = RFC2822AddressParser.STRICT.parse(to)
            !config.domainBlacklist.contains(parsedAddress.domain)
        } catch (ex: Exception) {
            false
        }
    }
}