package com.geekbeast.rhizome.pods;

import com.geekbeast.rhizome.configuration.websockets.WebSocketConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@Import( { WebSocketConfig.class } )
public class WebSocketPod {

    @Bean
    public WebSocketConfig getWebSocketConfig() {
        return new WebSocketConfig();
    }
}
