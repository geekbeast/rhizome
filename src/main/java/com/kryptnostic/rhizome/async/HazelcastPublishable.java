package com.kryptnostic.rhizome.async;

/**
 * This interface makes it easy to publish messages to Hazelcast. 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
public interface HazelcastPublishable {
    default boolean broadcastToHazelcast() {
        return true;
    }
}
