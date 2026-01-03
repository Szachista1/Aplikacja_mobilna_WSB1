package com.example.quizzwsb

import retrofit2.http.GET

interface ApiService {
    @GET("api/questions")
    suspend fun getQuestions(): List<Question>
}
