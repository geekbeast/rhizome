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

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Preconditions
import jodd.mail.EmailAttachment
import org.apache.commons.lang3.StringUtils
import java.util.*

class RenderableEmailRequest(
        from: Optional<String>,
        to: List<String>,
        cc: Optional<List<String>>,
        bcc: Optional<List<String>>,
        val templatePath: String,
        val subject: Optional<String>,
        val templateObjs: Optional<Any>,
        @JsonProperty(ATTACHMENTS_FIELD) val byteArrayAttachment: Optional<Array<EmailAttachment<*>>>,
        @JsonProperty(ATTACHMENT_PATHS_FIELD) val attachmentPaths: Optional<Array<String>>
) : EmailRequest(from, to, cc, bcc) {
    init {
        Preconditions.checkArgument(StringUtils.isNotBlank(templatePath))
    }



    companion object {
        private const val ATTACHMENTS_FIELD = "attachments"
        private const val ATTACHMENT_PATHS_FIELD = "attachmentPaths"
        @JvmStatic
        fun fromEmailRequest(
                subject: String,
                templatePath: String,
                templateObjs: Optional<Any>,
                attachmentPaths: Optional<Array<String>>,
                byteArrayAttachment: Optional<Array<EmailAttachment<*>>>,
                request: EmailRequest
        ): RenderableEmailRequest {
            return RenderableEmailRequest(
                    request.from,
                    request.to,
                    request.cc,
                    request.bcc,
                    templatePath,
                    Optional.of(subject),
                    templateObjs,
                    byteArrayAttachment,
                    attachmentPaths
            )
        }
    }

    override fun toString(): String {
        return "RenderableEmailRequest(templatePath='$templatePath', subject=$subject, templateObjs=$templateObjs, byteArrayAttachment=$byteArrayAttachment, attachmentPaths=$attachmentPaths)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RenderableEmailRequest) return false
        if (!super.equals(other)) return false

        if (templatePath != other.templatePath) return false
        if (subject != other.subject) return false
        if (templateObjs != other.templateObjs) return false
        if (byteArrayAttachment != other.byteArrayAttachment) return false
        if (attachmentPaths != other.attachmentPaths) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + templatePath.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + templateObjs.hashCode()
        result = 31 * result + byteArrayAttachment.hashCode()
        result = 31 * result + attachmentPaths.hashCode()
        return result
    }
}