package com.example.quizzwsb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzwsb.ui.theme.QuizzWSBTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Przywrócona, brakująca definicja klasy UserAnswerRecord
data class UserAnswerRecord(val question: Question, val selectedAnswer: Answer)

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
    var currentScreen by remember { mutableStateOf("category_selection") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var userResults by remember { mutableStateOf<List<UserAnswerRecord>?>(null) }

    when (currentScreen) {
        "category_selection" -> CategorySelectionScreen(
            apiService = apiService,
            onCategorySelected = { category ->
                selectedCategory = category
                currentScreen = "quiz"
            }
        )
        "quiz" -> QuizScreen(
            apiService = apiService,
            category = selectedCategory!!,
            onQuizFinished = { results ->
                userResults = results
                currentScreen = "results"
            },
            onNavigateBack = { currentScreen = "category_selection" }
        )
        "results" -> ResultsScreen(
            results = userResults!!,
            onFinish = {
                selectedCategory = null
                userResults = null
                currentScreen = "category_selection"
            }
        )
    }
}

@Composable
fun CategorySelectionScreen(apiService: ApiService, onCategorySelected: (Category) -> Unit) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            categories = apiService.getCategories()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.pobrane), contentDescription = "Logo", modifier = Modifier.padding(vertical = 32.dp).height(80.dp))
        Text("Wybierz kategorię quizu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoading) CircularProgressIndicator() else {
            categories.forEach { category ->
                Button(onClick = { onCategorySelected(category) }, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.8f)) {
                    Text(category.name, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun QuizScreen(
    apiService: ApiService,
    category: Category,
    onQuizFinished: (List<UserAnswerRecord>) -> Unit,
    onNavigateBack: () -> Unit
) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Answer?>(null) }
    val userAnswers = remember { mutableStateListOf<UserAnswerRecord>() }

    LaunchedEffect(category) {
        isLoading = true
        try {
            questions = apiService.getQuestions().filter { it.categoryId == category.id }
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (questions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Brak pytań w tej kategorii.", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Wróć")
            }
        }
    } else {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Pytanie ${currentQuestionIndex + 1}/${questions.size}")
                Text(question.question, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                question.answers.forEach { answer ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAnswer = answer }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAnswer?.id == answer.id,
                            onClick = { selectedAnswer = answer }
                        )
                        Text(answer.text, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedAnswer?.isCorrect == true) {
                            userAnswers.add(UserAnswerRecord(question, selectedAnswer!!))
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedAnswer = null
                            } else {
                                onQuizFinished(userAnswers)
                            }
                        } else {
                            // Nic się nie dzieje przy błędnej odpowiedzi
                        }
                    },
                    enabled = selectedAnswer != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(if (currentQuestionIndex < questions.size - 1) "Dalej" else "Zakończ i zobacz wyniki")
                }
            }
        } else {
            // Ten blok jest teraz zbędny, bo nawigacja dzieje się w przycisku
        }
    }
}

@Composable
fun ResultsScreen(results: List<UserAnswerRecord>, onFinish: () -> Unit) {
    val correctAnswersCount = results.count { it.selectedAnswer.isCorrect }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Podsumowanie", style = MaterialTheme.typography.displaySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Twój wynik: $correctAnswersCount z ${results.size}", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(results) { record ->
                val question = record.question
                val selected = record.selectedAnswer
                val correctAnswer = question.answers.first { it.isCorrect }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(question.question, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        question.answers.forEach { answer ->
                            val isSelected = answer.id == selected.id
                            val isCorrect = answer.isCorrect

                            val (icon, color) = when {
                                isCorrect -> Icons.Default.Check to Color.Green.copy(alpha = 0.2f)
                                isSelected -> Icons.Default.Close to Color.Red.copy(alpha = 0.2f)
                                else -> null to Color.Transparent
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().background(color).padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (icon != null) {
                                    Icon(imageVector = icon, contentDescription = null, tint = if (isCorrect) Color.DarkGray else Color.Red)
                                }
                                Text(answer.text, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Wróć do wyboru kategorii")
        }
    }
}
