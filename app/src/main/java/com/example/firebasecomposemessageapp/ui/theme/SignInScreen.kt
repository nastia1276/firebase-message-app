package com.example.firebasecomposemessageapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebasecomposemessageapp.auth.AuthViewModel

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit,
    viewModel: AuthViewModel
) {
    val error = viewModel.errorMessage.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.setError(null)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Button(onClick = onSignInClick) {
            Text(text = "Sign in with Google")
        }
    }
}
