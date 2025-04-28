package com.example.frontend_happygreen.repositories

import com.example.frontend_happygreen.api.OpenAIService
import com.example.frontend_happygreen.models.ChatCompletionRequest
import com.example.frontend_happygreen.models.Message

class ChatGPTRepository(private val openAIService: OpenAIService = OpenAIService.create()) {

    private val systemPrompt = """You are an assistant for a recycling and waste management app called 
        HappyGreen. Only answer questions related to recycling, waste management, sustainability, and 
        environmental topics. For any other questions, politely explain that you can only answer 
        questions about waste, recycling, and environmental sustainability. Keep answers concise 
        and practical."""

    suspend fun askAboutRecycling(question: String): String {
        try {
            val messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = question)
            )

            val request = ChatCompletionRequest(
                messages = messages
            )

            val response = openAIService.createChatCompletion(request = request)

            return response.choices.firstOrNull()?.message?.content
                ?: "I'm sorry, I couldn't generate a response about recycling."

        } catch (e: Exception) {
            return "Sorry, I encountered an error: ${e.message}"
        }
    }

    fun isRecyclingRelatedQuestion(question: String): Boolean {
        val keywords = listOf(
            "recycle", "waste", "trash", "garbage", "compost", "environment",
            "reuse", "sustainable", "green", "eco", "plastic", "paper", "glass",
            "metal", "disposal", "bin", "container", "pollution", "landfill"
        )

        return keywords.any { keyword ->
            question.lowercase().contains(keyword)
        }
    }
}