package com.kryptnostic.rhizome.mapstores.cassandra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface CassandraOptions {
    static enum ReplicationStrategy {
        SIMPLE
    };

    ReplicationStrategy strategy() default ReplicationStrategy.SIMPLE;

    int replicationFactor() default 2;

}
