package com.example.quizzwsb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzwsb.ui.theme.QuizzWSBTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val apiService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()
        Retrofit.Builder().baseUrl("http://10.0.2.2:3000/").client(client).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzWSBTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.LightGray) {
                    AppNavigation(apiService = apiService)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(apiService: ApiService) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    if (selectedCategory == null) {
        CategorySelectionScreen(apiService = apiService, onCategorySelected = { category ->
            selectedCategory = category
        })
    } else {
        QuizScreen(apiService = apiService, category = selectedCategory!!, onNavigateBack = {
            selectedCategory = null
        })
    }
}

@Composable
fun CategorySelectionScreen(apiService: ApiService, onCategorySelected: (Category) -> Unit) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            categories = apiService.getCategories()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Błąd połączenia"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Wybierz kategorię quizu", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Błąd: $errorMessage", color = Color.Red)
        } else {
            categories.forEach { category ->
                Button(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text(category.name, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun QuizScreen(apiService: ApiService, category: Category, onNavigateBack: () -> Unit) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Answer?>(null) }

    LaunchedEffect(category) {
        isLoading = true
        try {
            questions = apiService.getQuestions().filter { it.category == category.name }
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        // Ekran ładowania pytań
    } else if (questions.isNotEmpty()) {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Kategoria: ${category.name} | Pytanie ${currentQuestionIndex + 1}/${questions.size}")
                Text(question.question, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                question.answers.forEach { answer ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAnswer = answer }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAnswer?.id == answer.id, onClick = { selectedAnswer = answer })
                        Text(answer.text, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedAnswer?.isCorrect == true) score++
                        currentQuestionIndex++
                        selectedAnswer = null
                    },
                    enabled = selectedAnswer != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dalej")
                }
            }
        } else {
            // Ekran wyników
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text("Koniec Quizu!", style = MaterialTheme.typography.displaySmall)
                Text("Wynik: $score z ${questions.size}", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateBack) { Text("Wybierz inną kategorię") }
            }
        }
    } else {
        // Ekran na wypadek braku pytań w kategorii
    }
}
