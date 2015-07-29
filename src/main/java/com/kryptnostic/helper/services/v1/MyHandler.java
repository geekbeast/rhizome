package com.kryptnostic.helper.services.v1;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.geekbeast.rhizome.configuration.websockets.WebSocketConfig;

public class MyHandler extends TextWebSocketHandler {
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
		super.handleTextMessage(session, message);
		LoggerFactory.getLogger( WebSocketConfig.class ).debug("MESSAGE RECEIVED: {}", message.getPayload());
		TextMessage returnMessage = new TextMessage(message.getPayload() + "received!!!!!");
		session.sendMessage(returnMessage);
	}
}
