package com.example.firebasecomposemessageapp.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val isUserSignedIn = mutableStateOf(auth.currentUser != null)
    val errorMessage = mutableStateOf<String?>(null)

    fun setError(msg: String?) {
        errorMessage.value = msg
    }

    fun signIn(
        activity: Activity,
        request: BeginSignInRequest,
        webClientId: String,
        onResult: (IntentSenderRequest) -> Unit
    ) {
        val oneTapClient = Identity.getSignInClient(activity)

        val signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                onResult(IntentSenderRequest.Builder(result.pendingIntent).build())
            }
            .addOnFailureListener { e ->
                setError("Sign-in failed. Trying again...")

                val signUpRequest = BeginSignInRequest.Builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(webClientId)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()

                oneTapClient.beginSignIn(signUpRequest)
                    .addOnSuccessListener { result ->
                        onResult(IntentSenderRequest.Builder(result.pendingIntent).build())
                    }
                    .addOnFailureListener { err ->
                        setError("Google Sign-In error: ${err.localizedMessage}")
                    }
            }
    }

    fun handleSignInResult(activity: Activity, data: Intent?) {
        try {
            val credential = Identity.getSignInClient(activity).getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(firebaseCredential)
                .addOnSuccessListener {
                    isUserSignedIn.value = true
                }
                .addOnFailureListener { e ->
                    setError("Firebase sign-in error: ${e.localizedMessage}")
                }

        } catch (e: Exception) {
            setError("Sign-in cancelled or failed")
        }
    }

    fun signOut() {
        auth.signOut()
        isUserSignedIn.value = false
    }
}
