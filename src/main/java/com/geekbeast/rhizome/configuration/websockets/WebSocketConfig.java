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

//@Configuration
//@EnableWebSocket
//@Import( { HandshakeInterceptor.class } )
//public class WebSocketConfig implements WebSocketConfigurer {

@EnableAsync
@Configuration
//@EnableScheduling
//@ComponentScan("org.springframework.samples")
@EnableWebSocketMessageBroker
@Import( { HandshakeInterceptor.class } )
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	
//	@Async
//	@Bean
//	public ServletServerContainerFactoryBean createWebSocketContainer() {
//		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//		container.setMaxTextMessageBufferSize(8192);
//		container.setMaxBinaryMessageBufferSize(8192);
//		container.setAsyncSendTimeout(1000);
//		container.getObject().
//		return container;
//	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/one", "/two", "/three");
		config.setApplicationDestinationPrefixes("/sockettesting");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/sockettesting")
			.setHandshakeHandler(new DefaultHandshakeHandler(upgradeStrategy()))
			.addInterceptors(new HandshakeInterceptor())
			.setAllowedOrigins("*")
			.withSockJS();
	}

//	@Override
//	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
////		LoggerFactory.getLogger( WebSocketConfig.class ).debug("REGISTER WEB SOCKET HANDLERS");
//		registry.addHandler(myHandler(), "/")
//			.setAllowedOrigins("*")
//			.addInterceptors(new HandshakeInterceptor());
////			.withSockJS();
//	}
	
//	@Override
//	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		registry.addEndpoint("/socketone").withSockJS()
//			.setClientLibraryUrl("http://localhost:8000/");
//	}

	// @Bean
	// protected void configureStompEndpoints(StompEndpointRegistry registry) {
	// registry.addEndpoint("/sockettest").withSockJS();
	// }
	//
	// @Override
	// public void configureMessageBroker(MessageBrokerRegistry registry) {
	// registry.enableSimpleBroker("/sockettest/", "/test/");
	// registry.setApplicationDestinationPrefixes("/sockettest");
	// }

//	@Bean
//	public TextWebSocketHandler myHandler() {
//		return new MyHandler();
//	}

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

//	 @Bean
//	 public IntegrationWebSocketContainer serverWebSocketContainer() {
//	 return new ServerWebSocketContainer("/socketone");
//	 }

}
