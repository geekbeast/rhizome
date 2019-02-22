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

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.HttpURLConnection
import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ApiExceptions(
        @JsonProperty("type") private val type: String,
        @JsonProperty("code") private val code: Int) {
    RESOURCE_NOT_FOUND_EXCEPTION("resourceNotFound", HttpURLConnection.HTTP_NOT_FOUND),
    ILLEGAL_ARGUMENT_EXCEPTION("illegalArgument", HttpURLConnection.HTTP_BAD_REQUEST),
    ILLEGAL_STATE_EXCEPTION("illegalState", HttpURLConnection.HTTP_INTERNAL_ERROR),
    TYPE_EXISTS_EXCEPTION("typeExists", HttpURLConnection.HTTP_CONFLICT),
    FORBIDDEN_EXCEPTION("forbidden", HttpURLConnection.HTTP_FORBIDDEN),
    TOKEN_REFRESH_EXCEPTION("tokenNeedsRefresh", HttpURLConnection.HTTP_UNAUTHORIZED),
    AUTH0_TOKEN_EXCEPTION("tokenError", HttpURLConnection.HTTP_UNAUTHORIZED),
    OTHER_EXCEPTION("otherError", HttpURLConnection.HTTP_INTERNAL_ERROR);
}