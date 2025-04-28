// ChatViewModel.kt
package com.example.frontend_happygreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.repositories.ChatGPTRepository
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatGPTRepository = ChatGPTRepository()
) : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun sendMessage(message: String) {
        // Add user message to chat
        addMessage(ChatMessage.User(message))

        if (!repository.isRecyclingRelatedQuestion(message)) {
            addMessage(ChatMessage.Assistant("I can only answer questions related to recycling, waste management, and environmental sustainability. How can I help you with these topics?"))
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.askAboutRecycling(message)
                addMessage(ChatMessage.Assistant(response))
            } catch (e: Exception) {
                addMessage(ChatMessage.Assistant("Sorry, I encountered an error processing your question about recycling."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentList = _chatMessages.value ?: emptyList()
        _chatMessages.value = currentList + message
    }
}

sealed class ChatMessage {
    data class User(val text: String) : ChatMessage()
    data class Assistant(val text: String) : ChatMessage()
}