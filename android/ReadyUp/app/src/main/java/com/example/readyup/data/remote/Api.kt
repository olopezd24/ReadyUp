package com.example.readyup.data.remote

object Api {
    val service: ApiService by lazy {
        ApiClient.retrofit.create(ApiService::class.java)
    }
}