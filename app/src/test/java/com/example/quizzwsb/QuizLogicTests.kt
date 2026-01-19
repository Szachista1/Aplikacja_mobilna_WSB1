package com.example.quizzwsb

import kotlinx.coroutines.runBlocking
import org.junit.Assert.* 
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizLogicTests {

    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        apiService = Retrofit.Builder()
            .baseUrl("http://localhost:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Test
    fun `test_pobierania_kategorii_z_API()`() = runBlocking {
        // ACT (Działaj) - Pobierz kategorie z serwera
        val categories = apiService.getCategories()

        // ASSERT (Sprawdź) - Zweryfikuj, czy odpowiedź nie jest pusta
        assertNotNull("Odpowiedź z API nie powinna być nullem.", categories)
        assertTrue("API nie zwróciło żadnych kategorii. Sprawdź, czy serwer działa i czy tabela 'categories' w bazie danych nie jest pusta.", categories.isNotEmpty())

        // LOG (Zaloguj) - To pomoże Ci zobaczyć, co faktycznie przyszło z serwera
        val categoryNames = categories.map { "'${it.name}'" } // Dodajemy apostrofy, żeby zobaczyć ewentualne białe znaki
        println("Test zaliczony: Pomyślnie pobrano ${categories.size} kategorie.")
        println("Znalezione nazwy kategorii: ${categoryNames.joinToString(", ")}")
    }

    @Test
    fun `test_filtrowania_pytan_dla_kategorii_Budynek_na_danych_z_API()`() = runBlocking {
        val allQuestions = apiService.getQuestions()
        val budynekCategory = apiService.getCategories().firstOrNull { it.name == "Budynek" }

        assertNotNull("Kategoria 'Budynek' musi istnieć w bazie danych!", budynekCategory)

        val filteredQuestions = allQuestions.filter { it.categoryId == budynekCategory!!.id }

        assertEquals("Powinny zostać znalezione 2 pytania dla kategorii 'Budynek'", 2, filteredQuestions.size)
        assertTrue("Wszystkie odfiltrowane pytania powinny mieć kategorię 'Budynek'", 
                   filteredQuestions.all { it.category == "Budynek" })

        println("Test filtrowania dla kategorii 'Budynek' zaliczony.")
    }

     @Test
    fun `test_poprawnosci_danych_dla_pytania_o_liczbe_budynkow()`() = runBlocking {
        val allQuestions = apiService.getQuestions()
        val specificQuestion = allQuestions.find { it.question == "Ile budynków ma WSB" }

        assertNotNull("Pytanie 'Ile budynków ma WSB' musi istnieć w bazie!", specificQuestion)
        
        val correctAnswer = specificQuestion!!.answers.find { it.isCorrect }
        assertNotNull("Pytanie musi mieć poprawną odpowiedź", correctAnswer)
        assertEquals("Poprawna odpowiedź na to pytanie powinna brzmieć '3'", "3", correctAnswer!!.text)

         println("Test poprawności danych dla pytania o budynki zaliczony.")
    }
}
