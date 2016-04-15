package com.kryptnostic.rhizome.hazelcast.objects;

import com.datastax.driver.core.PagingState;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class PagedResponse<T> {

    private PagingState pagingState;
    private T           results;

    public PagedResponse( T results, PagingState pagingState ) {
        this.results = results;
        this.pagingState = pagingState;
    }

    public PagingState getPagingState() {
        return pagingState;
    }

    public T getResults() {
        return results;
    }

    public T serialize( ObjectDataOutput out ) {
        return results;
    }

    public void deserialize( ObjectDataInput input ) {

    }

}
