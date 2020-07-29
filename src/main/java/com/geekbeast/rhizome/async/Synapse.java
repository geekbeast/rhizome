package com.geekbeast.rhizome.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;

/**
 * This class acts as a synaptic bridge between Hazelcast and the local asynchronous event bus. 
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
@SuppressWarnings( "rawtypes" )
public class Synapse implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger( MessageListener.class );
    private final ITopic axon;
    private final AsyncEventBus dendrite;

    @SuppressWarnings( "unchecked" )
    public Synapse( ITopic axon, AsyncEventBus dendrite ) {
        this.dendrite = dendrite;
        this.axon = axon;

        axon.addMessageListener( this );
        logger.info( "Neural lace uplink connected. Incoming messages from Hazelcast cluster are being republished on local event bus." ); 
        dendrite.register( this );
        logger.info( "Neural lace downlink connected. Outgoing message from Eventbus will be re-broadcast to Hazelcast cluster." );
    }
    
    @SuppressWarnings( "unchecked" )
    @Subscribe
    public void integrateAndSignal( AlwaysPublishToHazelcast message ) {
        axon.publish( message );
    }
    
    @SuppressWarnings( "unchecked" )
    @Subscribe
    public void integrateAndSignal( HazelcastPublishable message ) {
        if( message.broadcastToHazelcast() ) {
            axon.publish( message );
        }
    }
    
    @Override
    public void onMessage( Message message ) {
        dendrite.post( message );
    }
}
