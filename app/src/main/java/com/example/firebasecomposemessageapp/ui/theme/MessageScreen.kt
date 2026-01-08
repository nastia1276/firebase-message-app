package com.example.firebasecomposemessageapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MessageScreen(
    onSignOutClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var message by remember { mutableStateOf("") }
    var latestMessage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            error = null
        }
    }

    // Listener for real-time updates
    LaunchedEffect(Unit) {
        firestore.collection("messages").document("latest")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = "Failed to load messages: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                latestMessage = snapshot?.getString("text") ?: ""
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SnackbarHost(hostState = snackbarHostState)

        Text(text = "Актуальне повідомлення:")
        Text(text = latestMessage, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Нове повідомлення") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            if (message.isBlank()) {
                error = "Повідомлення не може бути пустим"
                return@Button
            }

            firestore.collection("messages").document("latest")
                .set(mapOf("text" to message))
                .addOnFailureListener { e ->
                    error = "Помилка відправки: ${e.localizedMessage}"
                }

            message = ""
        }) {
            Text("Надіслати")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onSignOutClick) {
            Text("Вийти")
        }
    }
}

