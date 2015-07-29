package com.kryptnostic.helper.services.v1;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.geekbeast.rhizome.pods.RethinkDbPod;

@EnableAsync
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
	
//	@Override
//	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//		LoggerFactory.getLogger( HandshakeInterceptor.class ).debug("DOING HANDSHAKE STUFFS");
//		LoggerFactory.getLogger( HandshakeInterceptor.class ).debug("request: {}", request);
//		LoggerFactory.getLogger( HandshakeInterceptor.class ).debug("handler: {}", wsHandler);
//		LoggerFactory.getLogger( HandshakeInterceptor.class ).debug("attributes: {}", attributes);
//		Principal principal = request.getPrincipal();
//		return principal;
//	}
	
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception{
		return super.beforeHandshake(request, response, wsHandler, attributes);
	}
	
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception e) {
		super.afterHandshake(request, response, wsHandler, e);
	}

}
