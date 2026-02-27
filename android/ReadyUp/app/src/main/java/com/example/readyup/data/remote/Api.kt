package com.example.readyup.data.remote

object Api {
    val service: ApiService = ApiClient.retrofit.create(ApiService::class.java)
}