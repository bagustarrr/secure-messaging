package com.securemsg.config;

import com.securemsg.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatService chatService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Set prefix for messages from client to server (app destination)
        registry.setApplicationDestinationPrefixes("/app");
        // Enable a simple in-memory broker for broadcast and user-specific topics
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for user-specific messages (convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Intercept incoming STOMP messages to enforce access rules
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                    String dest = accessor.getDestination();
                    if (dest != null && dest.startsWith("/topic/chatroom/")) {
                        // Extract chatId from destination, e.g., "/topic/chatroom/12345"
                        String chatId = dest.substring("/topic/chatroom/".length());
                        String userIin = (accessor.getUser() != null ? accessor.getUser().getName() : null);
                        if (userIin == null || !chatService.isUserInChat(chatId, userIin)) {
                            throw new org.springframework.security.access.AccessDeniedException("Not allowed to subscribe to this chat");
                        }
                    }
                }
                return message;
            }
        });
    }
}
