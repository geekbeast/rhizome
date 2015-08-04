package com.geekbeast.rhizome.configuration.websockets;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.websocket.IntegrationWebSocketContainer;
import org.springframework.integration.websocket.ServerWebSocketContainer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.geekbeast.rhizome.pods.RethinkDbPod;
import com.kryptnostic.helper.services.v1.HandshakeInterceptor;
import com.kryptnostic.helper.services.v1.MyHandler;


@Configuration
@EnableWebSocketMessageBroker
@Import( { HandshakeInterceptor.class } )
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/proxy");
	}
	
	@Override 
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/")
			.setHandshakeHandler(new DefaultHandshakeHandler(upgradeStrategy()))
			.addInterceptors(new HandshakeInterceptor())
			.setAllowedOrigins("*")
			.withSockJS();
	}

	@Bean
	public RequestUpgradeStrategy upgradeStrategy() {
		WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
		policy.setInputBufferSize(8192);
		policy.setIdleTimeout(600000);
		return new JettyRequestUpgradeStrategy(new WebSocketServerFactory(
				policy));
	}

	@Bean
	public DefaultHandshakeHandler handshakeHandler() {
		return new DefaultHandshakeHandler(upgradeStrategy());
	}


}
