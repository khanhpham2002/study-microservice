package com.study.whiteboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Tiền tố cho các kênh người dùng đăng ký lắng nghe (Subscribe)
        config.enableSimpleBroker("/topic");
        // Tiền tố cho các request gửi từ Client lên Server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket chính thức
        registry.addEndpoint("/ws-whiteboard")
                .setAllowedOriginPatterns("*");

        // Endpoint WebSocket hỗ trợ SockJS fallback (cho các trình duyệt cũ / proxy)
        registry.addEndpoint("/ws-whiteboard")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
