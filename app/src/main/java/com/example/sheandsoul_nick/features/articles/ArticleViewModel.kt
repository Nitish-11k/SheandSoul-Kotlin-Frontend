package com.example.sheandsoul_nick.features.articles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.ArticleCategoryDto
import com.example.sheandsoul_nick.data.remote.ArticleDto
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.launch

sealed class DataState<T> {
    data class Success<T>(val data: T) : DataState<T>()
    data class Error<T>(val message: String) : DataState<T>()
    class Loading<T> : DataState<T>()
}

class ArticleViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    private val _categories = MutableLiveData<DataState<List<ArticleCategoryDto>>>()
    val categories: LiveData<DataState<List<ArticleCategoryDto>>> = _categories

    private val _selectedArticle = MutableLiveData<DataState<ArticleDto>>()
    val selectedArticle: LiveData<DataState<ArticleDto>> = _selectedArticle

    fun loadArticlesIfTokenAvailable() {
        // Only fetch if we have a token
        if (authViewModel.token != null) {
            fetchArticleCategories()
        }else{
            _categories.value = DataState.Error("Authentication token not available.")
        }
    }

    private fun fetchArticleCategories() {
        _categories.value = DataState.Loading()
        viewModelScope.launch {
            try {
                val response = apiService.getArticles()
                if (response.isSuccessful && response.body() != null) {
                    val articles: List<ArticleDto> = response.body()!!

                    // This block wraps the list of articles in a category for the UI
                    if (articles.isNotEmpty()) {
                        val articleCategory = ArticleCategoryDto(
                            name = "Recommended For You",
                            articles = articles
                        )
                        _categories.postValue(DataState.Success(listOf(articleCategory)))
                    } else {
                        _categories.postValue(DataState.Success(emptyList()))
                    }

                } else {
                    _categories.postValue(DataState.Error("Failed to load articles: ${response.message()}"))
                }
            } catch (e: Exception) {
                _categories.postValue(DataState.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }
    fun fetchArticleById(articleId: Long) {
        _selectedArticle.value = DataState.Loading()
        viewModelScope.launch {
            try {
                val response = apiService.getArticleById(articleId)
                if (response.isSuccessful && response.body() != null) {
                    _selectedArticle.postValue(DataState.Success(response.body()!!))
                } else {
                    _selectedArticle.postValue(DataState.Error("Failed to load article: ${response.message()}"))
                }
            } catch (e: Exception) {
                _selectedArticle.postValue(DataState.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }
}