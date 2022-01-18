package com.geekbeast.rhizome.configuration.websockets;

import com.geekbeast.helper.services.v1.v1.HandshakeInterceptor;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
@Import( { HandshakeInterceptor.class } )
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker( MessageBrokerRegistry registry ) {

        registry.enableSimpleBroker( "/topic" );
        registry.setApplicationDestinationPrefixes( "/proxy" );
    }

    @Override
    public void registerStompEndpoints( StompEndpointRegistry registry ) {

        registry
                .addEndpoint( "/" )
                .setHandshakeHandler( handshakeHandler() )
                .addInterceptors( new HandshakeInterceptor() )
                .setAllowedOrigins( "*" )
                .withSockJS();
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {

        WebSocketPolicy policy = new WebSocketPolicy( WebSocketBehavior.SERVER );
        policy.setInputBufferSize( 8192 );
        policy.setIdleTimeout( 600000 );

        return new DefaultHandshakeHandler( new JettyRequestUpgradeStrategy( policy ) );
    }

    @Bean
    public WebSocketStompClient client() {

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();

        WebSocketStompClient stompClient = new WebSocketStompClient( new StandardWebSocketClient() );
        stompClient.setMessageConverter( new StringMessageConverter() );
        stompClient.setTaskScheduler( taskScheduler );
        stompClient.setReceiptTimeLimit( 5000 );

        return stompClient;
    }

}
