package com.example.quizzwsb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quizzwsb.ui.theme.QuizzWSBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuizzWSBTheme {
                // 1. Używamy Box jako głównego kontenera dla tła (Gradientu)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Blue, Color.White)
                            )
                        )
                ) {
                    // 2. Scaffold musi być przezroczysty, żeby było widać Box pod spodem
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        // 3. Twoja nawigacja
                        AppNavigation(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }

    @Composable
    fun ScreenWithColorBackground() {
        // Surface zajmuje całą dostępną przestrzeń i ustawia kolor tła
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF123456) // Twój kolor (lub Color.Blue)
        ) {

        }
    }

    @Composable
    fun AppNavigation(modifier: Modifier = Modifier) {
        var currentScreen by remember { mutableStateOf("main") }

        when (currentScreen) {
            "main" -> Greeting(
                modifier = modifier,
                onNavigateToCheckbox = { currentScreen = "checkbox" }
            )

            "checkbox" -> CheckboxScreen(
                modifier = modifier,
                onNavigateBack = { currentScreen = "main" }
            )
        }
    }

    @Composable
    fun Greeting(modifier: Modifier = Modifier, onNavigateToCheckbox: () -> Unit) {
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.pobrane),
                contentDescription = "WSB Logo",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(0.dp)
                    .size(340.dp)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onNavigateToCheckbox) {
                    Text("Budynek")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text("Button 2")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text("Button 3")
                }
            }
        }
    }

    @Composable
    fun CheckboxScreen(modifier: Modifier = Modifier, onNavigateBack: () -> Unit) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (checkbox1, onCheckbox1Change) = remember { mutableStateOf(false) }
            val (checkbox2, onCheckbox2Change) = remember { mutableStateOf(false) }
            val (checkbox3, onCheckbox3Change) = remember { mutableStateOf(false) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checkbox1, onCheckedChange = onCheckbox1Change)
                Text("Opcja 1")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checkbox2, onCheckedChange = onCheckbox2Change)
                Text("Opcja 2")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checkbox3, onCheckedChange = onCheckbox3Change)
                Text("Opcja 3")
            }
            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Powrót")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        QuizzWSBTheme {
            Greeting(onNavigateToCheckbox = {})
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun CheckboxScreenPreview() {
        QuizzWSBTheme {
            CheckboxScreen(onNavigateBack = {})
        }
    }
}
