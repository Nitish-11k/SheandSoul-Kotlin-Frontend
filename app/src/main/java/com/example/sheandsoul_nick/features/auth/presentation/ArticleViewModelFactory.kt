package com.example.sheandsoul_nick.features.articles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.ArticleCategoryDto
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.launch

// A generic class to represent the state of a network request
sealed class DataState<T> {
    data class Success<T>(val data: T) : DataState<T>()
    data class Error<T>(val message: String) : DataState<T>()
    class Loading<T> : DataState<T>()
}

// MODIFY THE CONSTRUCTOR here to accept AuthViewModel
class ArticleViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    // This line now correctly uses the passed-in authViewModel
    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    private val _categories = MutableLiveData<DataState<List<ArticleCategoryDto>>>()
    val categories: LiveData<DataState<List<ArticleCategoryDto>>> = _categories

    init {
        fetchArticleCategories()
    }

    private fun fetchArticleCategories() {
        _categories.value = DataState.Loading<List<ArticleCategoryDto>>()
        viewModelScope.launch {
            try {
                val response = apiService.getArticles()
                if (response.isSuccessful && response.body() != null) {
                    _categories.postValue(DataState.Success(response.body()!!))
                } else {
                    _categories.postValue(DataState.Error<List<ArticleCategoryDto>>("Failed to load articles"))
                }
            } catch (e: Exception) {
                _categories.postValue(DataState.Error<List<ArticleCategoryDto>>(e.message ?: "An error occurred"))
            }
        }
    }
}