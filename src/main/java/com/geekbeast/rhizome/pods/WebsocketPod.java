package com.geekbeast.rhizome.pods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import com.geekbeast.rhizome.configuration.websockets.WebSocketConfig;
 
@EnableAsync
@Configuration
@Import( { WebSocketConfig.class } )
public class WebsocketPod {	
	
	@Bean
	public WebSocketConfig getWebSocketConfig() {
		return new WebSocketConfig();
	}
}
