package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Set;

public interface SetProxy<T> extends Set<T> {

    Class<T> getType();

}
