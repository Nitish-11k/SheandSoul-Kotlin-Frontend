package com.example.sheandsoul_nick.data.remote

import com.google.gson.annotations.SerializedName

data class MusicDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("url") val audioUrl: String
)