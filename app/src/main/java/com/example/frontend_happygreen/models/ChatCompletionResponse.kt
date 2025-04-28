package com.example.frontend_happygreen.models

data class ChatCompletionResponse(
    val id: String,
    val `object`: String,  // Using backticks to escape the keyword
    val created: Long,
    val model: String,
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)