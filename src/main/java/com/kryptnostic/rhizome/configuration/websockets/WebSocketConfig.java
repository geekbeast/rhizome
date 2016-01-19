package com.kryptnostic.rhizome.configuration.websockets;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.kryptnostic.helper.services.v1.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@Import( { HandshakeInterceptor.class } )
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker( MessageBrokerRegistry config ) {
        config.enableSimpleBroker( "/topic" );
        config.setApplicationDestinationPrefixes( "/proxy" );
    }

    @Override
    public void registerStompEndpoints( StompEndpointRegistry registry ) {
        registry.addEndpoint( "/" )
                .setHandshakeHandler( new DefaultHandshakeHandler( upgradeStrategy() ) )
                .addInterceptors( new HandshakeInterceptor() )
                .setAllowedOrigins( "*" )
                .withSockJS();
    }

    @Bean
    public RequestUpgradeStrategy upgradeStrategy() {
        WebSocketPolicy policy = new WebSocketPolicy( WebSocketBehavior.SERVER );
        policy.setInputBufferSize( 8192 );
        policy.setIdleTimeout( 600000 );
        return new JettyRequestUpgradeStrategy( new WebSocketServerFactory(
                policy ) );
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler( upgradeStrategy() );
    }

    @Bean
    public WebSocketStompClient client() {
        WebSocketClient transport = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient( transport );
        stompClient.setMessageConverter( new StringMessageConverter() );
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        stompClient.setTaskScheduler( taskScheduler );
        stompClient.setReceiptTimeLimit( 5000 );
        return stompClient;
    }

}
