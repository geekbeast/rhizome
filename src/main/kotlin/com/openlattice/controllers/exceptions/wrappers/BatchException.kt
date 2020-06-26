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

package com.openlattice.controllers.exceptions.wrappers

import com.openlattice.controllers.util.ApiExceptions
import org.springframework.http.HttpStatus

class BatchException(private val statusCode: HttpStatus): RuntimeException() {
    private val serialVersionUID = 7632884063119454460L
    private var errors = ErrorsDTO()

    constructor(errors: ErrorsDTO) : this( HttpStatus.INTERNAL_SERVER_ERROR ) {
        this.errors = errors
    }

    constructor(errorsList: List<ErrorDTO>): this( HttpStatus.INTERNAL_SERVER_ERROR ) {
        this.errors.setErrors(errorsList)
    }

    constructor(errors: ErrorsDTO, statusCode: HttpStatus): this( statusCode ) {
        this.errors = errors
    }

    constructor(errorsList: List<ErrorDTO>, statusCode: HttpStatus): this( statusCode ) {
        this.errors.setErrors(errorsList)
    }

    fun getErrors(): ErrorsDTO {
        return errors
    }

    fun addError(error: ApiExceptions, message: String) {
        errors.addError(error, message)
    }

    fun setErrors(errors: ErrorsDTO) {
        this.errors = errors
    }

    fun setErrors(list: List<ErrorDTO>) {
        this.errors.setErrors(list)
    }

    fun getStatusCode(): HttpStatus {
        return statusCode
    }

}