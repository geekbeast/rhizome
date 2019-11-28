package com.kryptnostic.rhizome.configuration.cassandra;

import com.kryptnostic.rhizome.cassandra.TableDef;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface TableDefSource extends Supplier<Stream<TableDef>> {

    public static TableDefSource wrap( Supplier<Stream<TableDef>> s ) {
        return s::get;
    }
}
