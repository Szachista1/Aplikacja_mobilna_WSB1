package com.example.quizzwsb

import retrofit2.http.GET

// Ten plik zawiera teraz tylko interfejs. Modele są w pliku Models.kt

interface ApiService {
    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/questions")
    suspend fun getQuestions(): List<Question>
}
