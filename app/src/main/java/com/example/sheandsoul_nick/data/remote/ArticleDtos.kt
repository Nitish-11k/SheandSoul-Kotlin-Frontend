package com.example.sheandsoul_nick.data.remote

import com.google.gson.annotations.SerializedName

data class ArticleDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("imageUrl")
    val imageUrl: String?, // URL for the article image
    @SerializedName("content")
    val content: String? // The full article content

)

data class ArticleCategoryDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("articles")
    val articles: List<ArticleDto>
)