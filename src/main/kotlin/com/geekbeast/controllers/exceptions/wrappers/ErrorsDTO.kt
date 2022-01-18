/*
 * Copyright (C) 2019. OpenLattice, Inc.
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

package com.geekbeast.controllers.exceptions.wrappers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.geekbeast.controllers.util.ApiExceptions
import java.util.ArrayList

class ErrorsDTO() {
    @JsonProperty("errors") var errors = ArrayList<ErrorDTO>()

    constructor(error: ApiExceptions, message: String): this() {
        this.addError(error, message)
    }

    fun addError(error: ApiExceptions, message: String) {
        errors.add(ErrorDTO(error, message))
    }

    fun setErrors(errors: List<ErrorDTO>) {
        this.errors = ArrayList(errors)
    }

    override fun toString(): String {
        return "ErrorsDTO [errors=$errors]"
    }

    @JsonIgnore
    fun isEmpty(): Boolean {
        return errors.isEmpty()
    }

}