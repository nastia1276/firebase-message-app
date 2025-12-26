package com.example.firebasecomposemessageapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.firebasecomposemessageapp.auth.AuthViewModel
import com.example.firebasecomposemessageapp.ui.theme.MessageScreen
import com.example.firebasecomposemessageapp.ui.theme.SignInScreen
import com.example.firebasecomposemessageapp.ui.theme.FirebaseComposeMessageAppTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                authViewModel.handleSignInResult(this, result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        setContent {
            FirebaseComposeMessageAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (authViewModel.isUserSignedIn.value) {
                        MessageScreen(onSignOutClick = { authViewModel.signOut() })
                    } else {
                        SignInScreen(
                            onSignInClick = {
                                authViewModel.signIn(
                                    this,
                                    signInRequest,
                                    getString(R.string.default_web_client_id)
                                ) { request ->
                                    signInLauncher.launch(request)
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
