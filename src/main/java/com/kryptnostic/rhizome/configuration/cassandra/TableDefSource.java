package com.kryptnostic.rhizome.configuration.cassandra;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.kryptnostic.rhizome.cassandra.TableDef;

public interface TableDefSource extends Supplier<Stream<TableDef>> {

    public static TableDefSource wrap( Supplier<Stream<TableDef>> s ) {
        return () -> s.get();
    }
}
