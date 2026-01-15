package com.example.quizzwsb

// Model dla odpowiedzi
data class Answer(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

// Model dla pytania, zawiera teraz również categoryId
data class Question(
    val id: Int,
    val question: String,
    val categoryId: Int,
    val category: String,
    val answers: List<Answer>
)

// Model dla kategorii
data class Category(
    val id: Int,
    val name: String
)
