package com.kryptnostic.rhizome.mapstores.cassandra;

import org.junit.Assert;
import org.junit.Test;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

public class TestCassandraKeyMadness {
    @Table(
        name = "test" )
    public static class TestObj implements CassandraKey {
        @PartitionKey(
            value = 0 )
        private String a = "a";
        @PartitionKey(
            value = 1 )
        private String b = "b";
        @ClusteringColumn(
            value = 0 )
        private int    c = 3;
    }

    public static class TestObj2 extends TestObj {
        @ClusteringColumn(
            value = 1 )
        private int d = 4;
    }

    @Test
    public void test() {
        TestObj key = new TestObj2();
        Assert.assertEquals( "a", key.asPrimaryKey()[ 0 ] );
        Assert.assertEquals( "b", key.asPrimaryKey()[ 1 ] );
        Assert.assertEquals( 3, key.asPrimaryKey()[ 2 ] );
        Assert.assertEquals( 4, key.asPrimaryKey()[ 3 ] );
    }
}
