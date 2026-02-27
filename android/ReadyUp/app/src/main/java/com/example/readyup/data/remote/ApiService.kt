package com.example.readyup.data.remote

import com.example.readyup.data.model.GameListResponse
import com.example.readyup.data.model.LoginRequest
import com.example.readyup.data.model.MeResponse
import com.example.readyup.data.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("games")
    suspend fun getGames(
        @Query("q") q: String? = null,
        @Query("sort") sort: String? = null,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): GameListResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @GET("me")
    suspend fun me(@Header("Authorization") bearerToken: String): MeResponse
}