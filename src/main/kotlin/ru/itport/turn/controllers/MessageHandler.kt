package ru.itport.turn.controllers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class MessageHandler : TextWebSocketHandler() {

    private val queue: HashMap<String,ArrayList<TextMessage>> = HashMap()
    private val users: HashMap<String,HashMap<String,String>> = HashMap()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val messageJson = ObjectMapper().readTree(message.payload)
        when (messageJson.get("type").asText()) {
            "get_messages" -> {
                if (messageJson.has("from")) {
                    val from = messageJson.get("from").asText()
                    if (queue.containsKey(from)) queue[from]!!.forEach { session.sendMessage(it) }
                    queue[from] = ArrayList()
                }
            }
            "update_user_profile" -> {
                updateUser(messageJson)
            }
            "request_users_list" -> {
                val from = messageJson.get("from").asText();
                if (queue[from] == null) queue[from] = ArrayList()
                queue[from]!!.add(TextMessage(ObjectMapper().writeValueAsString(
                    hashMapOf(
                        "list" to users.values.map { it },
                        "type" to "users_list"
                    )
                )))
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

    private fun updateUser(data: JsonNode) {
        val fields = arrayOf("id","name","image","updatedAt")
        var dataChanged = false
        (users[data.get("id").asText()] ?: HashMap()).apply {
            fields.forEach {
                if (!data.get(it).asText().equals(this[it] ?: "")) {
                    set(it,data.get(it).asText())
                    dataChanged = true
                }
            }
        }.also {
            users[it["id"]!!.toString()] = it
            if (queue[it["id"]!!] == null) queue[it["id"]!!] = ArrayList()
            if (dataChanged) notifyUserChanged(it)
        }
    }

    private fun notifyUserChanged(data:HashMap<String,String>) {
        data.toMutableMap().apply {
            set("from",data["id"].toString())
            set("type","update_user_profile")
        }.also {message ->
            queue.keys.forEach {
                queue[it]!!.add(TextMessage(ObjectMapper().writeValueAsString(message)))
            }
        }
    }
}
