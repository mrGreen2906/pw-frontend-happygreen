package com.example.frontend_happygreen.models

data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 500
)

data class Message(
    val role: String,
    val content: String
)