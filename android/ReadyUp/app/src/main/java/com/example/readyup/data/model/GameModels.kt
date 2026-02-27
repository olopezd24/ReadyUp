package com.example.readyup.data.model

data class GameListResponse(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val results: List<GameListItem>
)

data class GameListItem(
    val id: Int,
    val title: String,
    val coverUrl: String? = null,
    val releaseDate: String? = null,
    val avgRating: Double? = null
)
