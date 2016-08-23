package com.kryptnostic.rhizome.mapstores.cassandra;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public interface CassandraKey {
    static Logger                               logger       = LoggerFactory.getLogger( CassandraKey.class );
    static ConcurrentMap<Class<?>, List<Field>> _methodCache = Maps.newConcurrentMap();

    default Object[] asPrimaryKey() {
        Class<?> clazz = getClass();
        List<Field> fields = _methodCache.get( clazz );
        if ( fields == null ) {
            List<Field> pkms = FieldUtils.getFieldsListWithAnnotation( clazz, PartitionKey.class );
            List<Field> ccms = FieldUtils.getFieldsListWithAnnotation( clazz, ClusteringColumn.class );
            int pkmCount = pkms.size();
            Field[] _fields = new Field[ pkms.size() + ccms.size() ];
            pkms.forEach( pkm -> _fields[ pkm.getAnnotation( PartitionKey.class ).value() ] = pkm );
            ccms.forEach( ccm -> _fields[ pkmCount + ccm.getAnnotation( ClusteringColumn.class ).value() ] = ccm );
            fields = Arrays.asList( _fields );
            _methodCache.put( clazz, fields );
        }
        return Lists.transform( fields, f -> {
            try {
                return FieldUtils.readField( f, this, true );
            } catch ( IllegalAccessException | IllegalArgumentException e ) {
                logger.error( "I've got a bad feeling about this. ", e );
                return null;
            }
        } ).toArray();
    }
    
}
