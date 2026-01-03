package com.example.quizzwsb

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quizzwsb.ui.theme.QuizzWSBTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val apiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzWSBTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray
                ) {
                    AppNavigation(apiService = apiService)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(apiService: ApiService) {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> GreetingScreen(
            onNavigateToQuestions = { currentScreen = "questions" }
        )
        "questions" -> QuestionsScreen(
            apiService = apiService,
            onNavigateBack = { currentScreen = "main" }
        )
    }
}

@Composable
fun GreetingScreen(onNavigateToQuestions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz WSB", style = MaterialTheme.typography.displayMedium, color = Color.Blue)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToQuestions,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text("BUDYNEK (Pobierz pytania)")
        }
    }
}

@Composable
fun QuestionsScreen(apiService: ApiService, onNavigateBack: () -> Unit) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            questions = apiService.getQuestions()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Nieznany błąd połączenia"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lista Pytań", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Text("Łączenie z serwerem...", modifier = Modifier.padding(top = 8.dp))
        } else if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BŁĄD POŁĄCZENIA", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                    Text(errorMessage!!, color = Color.Black)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(questions) { singleQuestion ->
                    Card(
                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = singleQuestion.question, style = MaterialTheme.typography.titleMedium)
                            for (ans in singleQuestion.answers) {
                                Text(text = "• $ans", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Wróć")
        }
    }
}
