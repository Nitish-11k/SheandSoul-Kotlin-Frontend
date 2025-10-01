package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.ChatRequest
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to represent a single message in the UI
data class ChatMessage(val text: String, val isFromUser: Boolean)

// ViewModel to manage the chat state and logic
class ChatViewModel(authViewModel: AuthViewModel) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    var isLoading = mutableStateOf(false)
        private set

    init {
        // Add an initial greeting from the bot
        _messages.value = listOf(ChatMessage("Hello! How can I help you today?", isFromUser = false))
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading.value) return

        // Add user's message to the list immediately
        _messages.value = _messages.value + ChatMessage(text, isFromUser = true)
        isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.sendMessageToChat(ChatRequest(message = text))
                if (response.isSuccessful && response.body() != null) {
                    // Add bot's response
                    _messages.value = _messages.value + ChatMessage(response.body()!!.response, isFromUser = false)
                } else {
                    _messages.value = _messages.value + ChatMessage("Sorry, something went wrong. Please try again.", isFromUser = false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Error: Could not connect to the server.", isFromUser = false)
            } finally {
                isLoading.value = false
            }
        }
    }
}

// Factory to create the ChatViewModel with the required AuthViewModel dependency
class ChatViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}