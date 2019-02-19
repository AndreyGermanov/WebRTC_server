package ru.itport.turn.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


class MessageHandler : TextWebSocketHandler() {

    private val queue: HashMap<String,ArrayList<TextMessage>> = HashMap()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val messageJson = ObjectMapper().readTree(message.payload)
        when (messageJson.get("type").asText()) {
            "get_messages" -> {
                if (messageJson.has("from")) {
                    val from = messageJson.get("from").asText()
                    if (queue.containsKey(from)) queue[from]!!.forEach { session.sendMessage(it) }
                    queue.remove(from)
                }
            }
            else -> {
                if (messageJson.has("to")) {
                    val to = messageJson.get("to").asText()
                    if (queue[to] == null) queue[to] = ArrayList()
                    queue[to]!!.add(message)
                }
            }
        }
    }
}
