package com.kryptnostic.rhizome.maptores;

import java.util.List;
import java.util.Random;

import jersey.repackaged.com.google.common.collect.Lists;

import org.hyperdex.client.Client;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mapstores.MagicHyperdexStore;

public class MagicHyperdexStoresTest {
    private static Client                                                                                   c;
    private static MagicHyperdexStore<ParameterizedObject<List<Integer>>, ParameterizedObject<List<String>>> store;
    static {
        HyperdexPreconfigurer.configure();
    }
    @BeforeClass
    public static void setup() {
        c = new Client(
                "batman",
                1982 );
        TypeReference<ParameterizedObject<List<String>>> reference = new TypeReference<ParameterizedObject<List<String>>>() {}; 
        store = new MagicHyperdexStore<ParameterizedObject<List<Integer>>, ParameterizedObject<List<String>>>("configurations", c, reference );
    }
    
    @Test
    public void testMapstore() {
        ParameterizedObject<List<Integer>> key = new ParameterizedObject<List<Integer>>();
        key.value = Lists.newArrayList();
        key.value.add( new Random().nextInt() );
        
        ParameterizedObject<List<String>> expected = new ParameterizedObject<List<String>>();
        expected.value = Lists.newArrayList();
        expected.value.add( "hello" );
        
        store.store( key, expected );
        ParameterizedObject<List<String>> actual = store.load( key );
        Assert.assertEquals( expected.value, actual.value );
    }

}
