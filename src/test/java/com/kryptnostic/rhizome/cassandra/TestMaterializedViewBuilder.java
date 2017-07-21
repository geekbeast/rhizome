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

package com.kryptnostic.rhizome.cassandra;

import com.datastax.driver.core.DataType;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class TestMaterializedViewBuilder {
    private static final Logger logger = LoggerFactory.getLogger( TestMaterializedViewBuilder.class );

    public static TableDef def = new TableDef() {
        @Override public String getKeyspace() {
            return "test";
        }

        @Override public String getName() {
            return "source";
        }

        @Override public CassandraTableBuilder getBuilder() {
            return base;
        }
    };

    public static ColumnDef p1 = new ColumnDef() {
        @Override public String cql() {
            return "p1";
        }

        @Override public DataType getType( Function<ColumnDef, DataType> typeResolver ) {
            return getType();
        }

        @Override public DataType getType() {
            return DataType.text();
        }
    };

    public static ColumnDef p2 = new ColumnDef() {
        @Override public String cql() {
            return "p2";
        }

        @Override public DataType getType( Function<ColumnDef, DataType> typeResolver ) {
            return getType();
        }

        @Override public DataType getType() {
            return DataType.text();
        }
    };

    public static ColumnDef c1 = new ColumnDef() {
        @Override public String cql() {
            return "c1";
        }

        @Override public DataType getType( Function<ColumnDef, DataType> typeResolver ) {
            return getType();
        }

        @Override public DataType getType() {
            return DataType.text();
        }
    };

    public static ColumnDef c2 = new ColumnDef() {
        @Override public String cql() {
            return "c2";
        }

        @Override public DataType getType( Function<ColumnDef, DataType> typeResolver ) {
            return getType();
        }

        @Override public DataType getType() {
            return DataType.text();
        }
    };

    public static final CassandraTableBuilder base = new CassandraTableBuilder( def )
            .partitionKey( p1, p2 )
            .clusteringColumns( c1, c2 );

    @Test
    public void testMaterializedView() {
        CassandraMaterializedViewBuilder mvb = new CassandraMaterializedViewBuilder( base, def.getKeyspace(), "mview" )
                .partitionKey( p2, c1 )
                .clusteringColumns( c2, p1 );

        String expected =
                "CREATE MATERIALIZED VIEW test.mview AS\n"
                        + "SELECT p2,c1,c2,p1\n"
                        + "FROM testsource\n"
                        + "WHERE p2 IS NOT NULL AND c1 IS NOT NULL AND c2 IS NOT NULL AND p1 IS NOT NULL\n"
                        + "PRIMARY KEY ((p2,c1),c2,p1)";
        String actual = mvb.buildCreateTableQuery();
        logger.info( "Query: {}", actual );
        Assert.assertEquals( expected, actual );
    }

}
