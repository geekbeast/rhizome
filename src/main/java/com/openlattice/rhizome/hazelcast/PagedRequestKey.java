/*
 * Copyright (C) 2017. OpenLattice, Inc
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

package com.openlattice.rhizome.hazelcast;

import com.datastax.driver.core.PagingState;

public class PagedRequestKey<K> {

    private K           key;
    private int         pageSize;
    private int         pageOffset;
    private PagingState pagingState;

    private PagedRequestKey( K key, int pageSize, int pageOffset ) {
        this.key = key;
        this.pageSize = pageSize;
        this.pageOffset = pageOffset;
    }

    private PagedRequestKey( PagingState pagingState ) {
        this.pagingState = pagingState;
    }

    public static <K> PagedRequestKey<K> nextPage( PagingState pagingState ) {
        return new PagedRequestKey<>( pagingState );
    }

    public static <K> PagedRequestKey<K> initialPage( K key, int pageSize, int pageOffset ) {
        return new PagedRequestKey<>( key, pageSize, pageOffset );
    }

    public K getKey() {
        return key;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public PagingState getPagingState() {
        return pagingState;
    }

    public boolean isExistingPagedQuery() {
        return pagingState != null;
    }
}
