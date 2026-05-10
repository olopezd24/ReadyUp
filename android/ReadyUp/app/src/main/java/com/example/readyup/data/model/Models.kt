package com.example.readyup.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Auth ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class RegisterRequest(val username: String, val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class TokenResponse(val access: String, val refresh: String)

@JsonClass(generateAdapter = true)
data class RefreshRequest(val refresh: String)

@JsonClass(generateAdapter = true)
data class RefreshResponse(val access: String)

@JsonClass(generateAdapter = true)
data class MeResponse(val id: Int, val username: String, val email: String)

// ── Games ─────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class GameListResponse(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val results: List<GameListItem>
)

@JsonClass(generateAdapter = true)
data class GameListItem(
    val id: Int,
    val title: String,
    @Json(name = "coverUrl") val coverUrl: String? = null,
    @Json(name = "releaseDate") val releaseDate: String? = null,
    @Json(name = "avgRating") val avgRating: Double? = null
)

@JsonClass(generateAdapter = true)
data class GameDetail(
    val id: Int,
    val title: String,
    val description: String? = null,
    @Json(name = "releaseDate") val releaseDate: String? = null,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    @Json(name = "coverUrl") val coverUrl: String? = null
)

// ── Reviews ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ReviewListResponse(val count: Int, val results: List<ReviewItem>)

@JsonClass(generateAdapter = true)
data class ReviewItem(
    val id: Int,
    val user: ReviewUser,
    val rating: Int,
    val text: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class ReviewUser(val id: Int, val username: String)

@JsonClass(generateAdapter = true)
data class MyReview(
    val id: Int,
    val rating: Int,
    val text: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class ReviewRequest(val rating: Int, val text: String)

@JsonClass(generateAdapter = true)
data class MyReviewListResponse(val count: Int, val results: List<MyReviewItem>)

@JsonClass(generateAdapter = true)
data class MyReviewItem(
    val game: GameRef,
    val rating: Int,
    val text: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class GameRef(val id: Int, val title: String)

// ── Status ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class StatusListResponse(val count: Int, val results: List<StatusItem>)

@JsonClass(generateAdapter = true)
data class StatusItem(
    val game: StatusGame,
    val status: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class StatusGame(
    val id: Int,
    val title: String,
    @Json(name = "cover_url") val coverUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GameStatusResponse(
    @Json(name = "game_id") val gameId: Int,
    val status: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class AddStatusRequest(@Json(name = "game_id") val gameId: Int, val status: String)

@JsonClass(generateAdapter = true)
data class UpdateStatusRequest(val status: String)

// ── Feed ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class FeedResponse(val count: Int, val results: List<FeedItem>)

@JsonClass(generateAdapter = true)
data class FeedItem(
    val user: ReviewUser,
    val game: GameRef,
    val rating: Int,
    val text: String,
    @Json(name = "updated_at") val updatedAt: String
)

// ── Follow ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class FollowListResponse(val count: Int, val results: List<FollowUser>)

@JsonClass(generateAdapter = true)
data class FollowUser(val id: Int, val username: String)

// ── Error ─────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ErrorResponse(val error: String)
