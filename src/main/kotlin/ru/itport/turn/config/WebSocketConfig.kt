package ru.itport.turn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.itport.turn.controllers.MessageHandler
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocket

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(messageHandler(), "/firexchat").setAllowedOrigins("*")
    }

    @Bean
    fun messageHandler(): WebSocketHandler {
        return MessageHandler()
    }
}