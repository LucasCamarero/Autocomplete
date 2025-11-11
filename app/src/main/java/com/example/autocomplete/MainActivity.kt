package com.example.autocomplete

import android.R.attr.query
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.autocomplete.ui.theme.AutocompleteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutocompleteTheme {
                Ventana()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ventana() {
    var lista = listOf<String>("Hola", "Adios", "Coca cola")
    var frase by remember { mutableStateOf("") }
    var listaSugerencias = remember { mutableListOf<String>() }
    //var listaSugerencias by remember { mutableStateOf(listOf<String>()) }

    // Ejemplo de autocompletado hecho a mano
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Búsqueda de sugerencias",
            modifier = Modifier
                .padding(bottom = 10.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Column {
            TextField(
                value = frase,
                onValueChange = { texto ->
                    frase = texto

                    if (texto.isNotBlank()) {
                        listaSugerencias.clear()
                        listaSugerencias.addAll(
                            lista.filter { it.contains(texto, ignoreCase = true) }
                        )
                    } else {
                        listaSugerencias.clear()
                    }
                },
                label = {Text("Buscar")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            listaSugerencias.forEach { texto ->
                Text(
                    texto,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .clickable{frase = texto
                            listaSugerencias.clear()},
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Ejemplo de autocompletado con SearchBar
        var query by remember{mutableStateOf("")}
        var active by remember{mutableStateOf(false)}

        Column() {
            SearchBar(
                query = query,
                onQueryChange = {query = it
                    if (query.isNotBlank()) {
                        listaSugerencias.clear()
                        listaSugerencias.addAll(
                            lista.filter { it.contains(query, ignoreCase = true) }
                        )
                    } else {
                        listaSugerencias.clear()
                    }},
                onSearch = {active = false},
                active = active, // sirve para desplegar el menú
                onActiveChange = {active = it},
                placeholder = {Text("Buscar")}
            ) {
                listaSugerencias.forEach { texto ->
                    Text(
                        texto,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .clickable{query = texto
                                listaSugerencias.clear()},
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Pide permiso para el micrófono
@Composable
fun pedirPermiso() {
    var resultado = ""
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            resultado = "Permiso de micrófono denegado"
        }
    }

    val context = LocalContext.current
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }
    }

    var escuchando by remember{mutableStateOf(true)}

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                resultado = "Escuchando..."
            }

            override fun onResults(results: Bundle?) {
                val texto = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                texto?.let {
                    query = it
                    resultado = "Dijiste: $it"
                }
                escuchando = false
            }


            override fun onError(error: Int) {
                resultado = "Error: $error"
                escuchando = false
            }

            override fun onEndOfSpeech() {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
        }
        speechRecognizer.setRecognitionListener(listener)


        onDispose {
            speechRecognizer.destroy()
        }
    }
}
