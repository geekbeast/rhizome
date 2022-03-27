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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.apache.commons.lang3.StringUtils
import java.util.*

open class EmailRequest(
    val from: Optional<String> = Optional.empty(),
    @SuppressFBWarnings(
        value = ["EI_EXPOSE_REP"],
        justification = "Only used internally hopefully not abused."
    )
    val to: List<String>,
    val cc: List<String> = emptyList(),
    val bcc: List<String> = emptyList(),
    val subject: String = "",
    val body: String = "",
    val html: Boolean = false
) {
    init {
        check(to.all { StringUtils.isNotBlank(it) }) { "All to address must not be blank. " }
        check(to.isNotEmpty()) { "At least one to address must be specified." }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmailRequest) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (cc != other.cc) return false
        if (bcc != other.bcc) return false
        if (subject != other.subject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        result = 31 * result + bcc.hashCode()
        result = 31 * result + subject.hashCode()
        return result
    }

    override fun toString(): String {
        return "EmailRequest(from=$from, to=$to, cc=$cc, bcc=$bcc, subject='$subject')"
    }

}