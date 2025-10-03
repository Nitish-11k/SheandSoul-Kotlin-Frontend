package com.example.sheandsoul_nick.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.CreateNoteRequest
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import com.example.sheandsoul_nick.data.remote.UserNoteDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State for the NoteScreen
sealed class NoteUiState {
    object Loading : NoteUiState()
    data class Success(val notes: List<UserNoteDto>) : NoteUiState()
    data class Error(val message: String) : NoteUiState()
}

class NoteViewModel(authViewModel: AuthViewModel) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getNotes()
    }

    fun getNotes() {
        viewModelScope.launch {
            _uiState.value = NoteUiState.Loading
            try {
                val response = apiService.getNotes()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = NoteUiState.Success(response.body()!!)
                } else {
                    _uiState.value = NoteUiState.Error("Failed to load notes.")
                }
            } catch (e: Exception) {
                _uiState.value = NoteUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // ✅ FIX: This function now accepts and sends both title and content.
    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                val response = apiService.createNote(CreateNoteRequest(title, content))
                if (response.isSuccessful) {
                    // Refresh the list to show the new note
                    getNotes()
                } else {
                    // Handle error (e.g., show a toast)
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteNote(noteId)
                if (response.isSuccessful) {
                    // Refresh the list
                    getNotes()
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}

// Factory to create the ViewModel with AuthViewModel dependency
class NoteViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}