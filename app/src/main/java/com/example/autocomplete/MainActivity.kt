package com.example.autocomplete

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material3.IconButton
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
import androidx.core.content.ContextCompat
import com.example.autocomplete.ui.theme.AutocompleteTheme
import android.Manifest
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow


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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutocompletadoArtesanal()

        SearchBar()

        SearchBarWithVoice()
    }
}

// Autocompletado hecho con diferentes composables
@Composable
fun AutocompletadoArtesanal() {
    var lista = listOf<String>("Hola", "Adios", "Coca cola")
    var frase by remember { mutableStateOf("") }
    var listaSugerencias = remember { mutableListOf<String>() }
    //var listaSugerencias by remember { mutableStateOf(listOf<String>()) }

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
}

//Autocompletado hecho con SearchBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    Column {
        var lista = listOf<String>("Hola", "Adios", "Coca cola")
        var listaSugerencias = remember { mutableListOf<String>() }
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

// Autocompletado con SearchBar y permiso de micrófono (NO ENTRA EN EXAMEN EL MICROFONO)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithVoice() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var escuchando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf("") }

    // Pedir permiso de micrófono
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            resultado = "Permiso de micrófono denegado"
        }
    }

    // Configurar SpeechRecognizer
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        }
    }

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

    Column(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { active = false },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Buscar...") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        )
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (!escuchando) {
                                speechRecognizer.startListening(intent)
                                escuchando = true
                            } else {
                                speechRecognizer.stopListening()
                                escuchando = false
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (escuchando) Icons.Default.PlayArrow else Icons.Default.Phone,
                        contentDescription = "Micrófono",
                        tint = if (escuchando) Color.Red else Color.Gray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val suggestions = listOf(
                "Hola", "Adios", "Coca cola"
            )

            val filtered = if (query.isNotBlank()) {
                suggestions.filter { it.contains(query, ignoreCase = true) }
            } else emptyList()

            filtered.forEach { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            query = suggestion
                            active = false
                        }
                        .padding(8.dp)
                )
            }
        }

        if (resultado.isNotBlank()) {
            Text(
                text = resultado,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
                color = Color.Gray
            )
        }
    }
}