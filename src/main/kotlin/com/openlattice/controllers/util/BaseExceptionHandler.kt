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
 *
 */

package com.openlattice.controllers.util

import com.openlattice.controllers.exceptions.ForbiddenException
import com.openlattice.controllers.exceptions.ResourceNotFoundException
import com.openlattice.controllers.exceptions.TypeExistsException
import com.openlattice.controllers.exceptions.wrappers.BatchException
import com.openlattice.controllers.exceptions.wrappers.ErrorsDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class BaseExceptionHandler {
    private val logger = LoggerFactory.getLogger(BaseExceptionHandler::class.java)

    @ExceptionHandler(NullPointerException::class, ResourceNotFoundException::class)
    fun handleNotFoundException(e: Exception): ResponseEntity<ErrorsDTO> {
        return handleException(e, HttpStatus.NOT_FOUND, ApiExceptions.RESOURCE_NOT_FOUND_EXCEPTION)
    }

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun handleIllegalArgumentException(e: Exception): ResponseEntity<ErrorsDTO> {
        return handleException(e, HttpStatus.BAD_REQUEST, ApiExceptions.ILLEGAL_ARGUMENT_EXCEPTION)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: Exception): ResponseEntity<ErrorsDTO> {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, ApiExceptions.ILLEGAL_STATE_EXCEPTION)
    }

    @ExceptionHandler(TypeExistsException::class)
    fun handleTypeExistsException(e: Exception): ResponseEntity<ErrorsDTO> {
        return handleException(e, HttpStatus.CONFLICT, ApiExceptions.TYPE_EXISTS_EXCEPTION)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleUnauthorizedExceptions(e: ForbiddenException): ResponseEntity<ErrorsDTO> {
        return handleException(e, HttpStatus.UNAUTHORIZED, ApiExceptions.FORBIDDEN_EXCEPTION)
    }

    @ExceptionHandler(BatchException::class)
    fun handleBatchExceptions(e: BatchException): ResponseEntity<ErrorsDTO> {
        logger.error("", e)
        return ResponseEntity(e.getErrors(), e.getStatusCode())
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(e: Exception): ResponseEntity<ErrorsDTO> {
        return handleException(
                e, HttpStatus.INTERNAL_SERVER_ERROR, ApiExceptions.OTHER_EXCEPTION, e.javaClass.simpleName + ": ")
    }

    private fun handleException(
            e: Exception,
            responseStatus: HttpStatus,
            responseException: ApiExceptions,
            prefixMessage: String = "",
            postFixMessage: String = ""): ResponseEntity<ErrorsDTO> {
        logger.error("", e)
        val errorMessage = e.message ?: ""
        return ResponseEntity(
                ErrorsDTO(responseException, prefixMessage + errorMessage + postFixMessage),
                responseStatus)
    }
}