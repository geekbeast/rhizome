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
    private val ERROR_MSG = ""

    @ExceptionHandler(NullPointerException::class, ResourceNotFoundException::class)
    fun handleNotFoundException(e: Exception): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return if (e.message != null) {
            ResponseEntity(ErrorsDTO(ApiExceptions.RESOURCE_NOT_FOUND_EXCEPTION, e.message!!), HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun handleIllegalArgumentException(e: Exception): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(
                ErrorsDTO(ApiExceptions.ILLEGAL_ARGUMENT_EXCEPTION, e.message!!),
                HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: Exception): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(
                ErrorsDTO(ApiExceptions.ILLEGAL_STATE_EXCEPTION, e.message!!),
                HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(TypeExistsException::class)
    fun handleTypeExistsException(e: Exception): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(
                ErrorsDTO(ApiExceptions.TYPE_EXISTS_EXCEPTION, e.message!!),
                HttpStatus.CONFLICT)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleUnauthorizedExceptions(e: ForbiddenException): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(
                ErrorsDTO(ApiExceptions.FORBIDDEN_EXCEPTION, e.message!!),
                HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(BatchException::class)
    fun handleBatchExceptions(e: BatchException): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(e.getErrors(), e.getStatusCode())
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(e: Exception): ResponseEntity<ErrorsDTO> {
        logger.error(ERROR_MSG, e)
        return ResponseEntity(
                ErrorsDTO(ApiExceptions.OTHER_EXCEPTION, e.javaClass.simpleName + ": " + e.message),
                HttpStatus.INTERNAL_SERVER_ERROR)
    }
}