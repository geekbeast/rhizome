package com.kryptnostic.rhizome.hazelcast.objects;

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
        return new PagedRequestKey<K>( pagingState );
    }

    public static <K> PagedRequestKey<K> initialPage( K key, int pageSize, int pageOffset ) {
        return new PagedRequestKey<K>( key, pageSize, pageOffset );
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
