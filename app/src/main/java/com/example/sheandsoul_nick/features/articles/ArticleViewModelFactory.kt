package com.example.sheandsoul_nick.features.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel

/**
 * Factory for creating an instance of ArticleViewModel with an AuthViewModel parameter.
 * This is the crucial link that gives the ArticleViewModel the user's login token.
 */
class ArticleViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}