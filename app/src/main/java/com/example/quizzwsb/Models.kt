package com.example.quizzwsb

// Model dla odpowiedzi, pasujący do serwera: { id, text, isCorrect }
data class Answer(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

// Model dla pytania, pasujący do serwera: { id, question, category, answers: [...] }
data class Question(
    val id: Int,
    val question: String,
    val category: String,
    val answers: List<Answer>
)

// Model dla kategorii, pasujący do serwera: { id, name }
data class Category(
    val id: Int,
    val name: String
)
