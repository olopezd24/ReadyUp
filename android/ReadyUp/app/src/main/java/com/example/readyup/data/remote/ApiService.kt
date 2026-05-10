package com.example.readyup.data.remote

import com.example.readyup.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): MeResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @GET("me")
    suspend fun me(): MeResponse

    // ── Games ─────────────────────────────────────────────────────────────────
    @GET("games")
    suspend fun getGames(
        @Query("q") q: String? = null,
        @Query("sort") sort: String? = null,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): GameListResponse

    @GET("games/{id}")
    suspend fun getGameDetail(@Path("id") id: Int): GameDetail

    // ── Reviews ───────────────────────────────────────────────────────────────
    @GET("games/{id}/reviews")
    suspend fun getGameReviews(
        @Path("id") id: Int,
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ReviewListResponse

    @GET("games/{id}/review")
    suspend fun getMyReview(@Path("id") id: Int): Response<MyReview>

    @POST("games/{id}/review")
    suspend fun createReview(@Path("id") id: Int, @Body body: ReviewRequest): MyReview

    @PUT("games/{id}/review")
    suspend fun updateReview(@Path("id") id: Int, @Body body: ReviewRequest): MyReview

    @DELETE("games/{id}/review")
    suspend fun deleteReview(@Path("id") id: Int): Response<Unit>

    @GET("me/reviews")
    suspend fun getMyReviews(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): MyReviewListResponse

    // ── Status ────────────────────────────────────────────────────────────────
    @GET("me/status")
    suspend fun getMyStatus(
        @Query("status") status: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): StatusListResponse

    @GET("me/status/{gameId}")
    suspend fun getGameStatus(@Path("gameId") gameId: Int): Response<GameStatusResponse>

    @POST("me/status")
    suspend fun addStatus(@Body body: AddStatusRequest): GameStatusResponse

    @PUT("me/status/{gameId}")
    suspend fun updateStatus(
        @Path("gameId") gameId: Int,
        @Body body: UpdateStatusRequest
    ): GameStatusResponse

    @DELETE("me/status/{gameId}")
    suspend fun deleteStatus(@Path("gameId") gameId: Int): Response<Unit>

    // ── Feed ──────────────────────────────────────────────────────────────────
    @GET("feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): FeedResponse

    // ── Follow ────────────────────────────────────────────────────────────────
    @POST("users/{userId}/follow")
    suspend fun follow(@Path("userId") userId: Int): Response<Unit>

    @DELETE("users/{userId}/follow")
    suspend fun unfollow(@Path("userId") userId: Int): Response<Unit>

    @GET("users/{userId}/followers")
    suspend fun getFollowers(@Path("userId") userId: Int): FollowListResponse

    @GET("users/{userId}/following")
    suspend fun getFollowing(@Path("userId") userId: Int): FollowListResponse
}