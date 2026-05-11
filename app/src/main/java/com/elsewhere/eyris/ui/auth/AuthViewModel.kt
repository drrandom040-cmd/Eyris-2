package com.elsewhere.eyris.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsewhere.eyris.data.repositories.UserRepository
import com.elsewhere.eyris.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuth()
    }

    private fun checkAuth() {
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser.uid)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val existingUser = userRepository.getUser(firebaseUser.uid)
                    val user = if (existingUser != null) {
                        existingUser.copy(lastOnline = System.currentTimeMillis())
                    } else {
                        User(
                            userId = firebaseUser.uid,
                            displayName = firebaseUser.displayName ?: "Google User",
                            email = firebaseUser.email ?: "user@example.com"
                        )
                    }
                    userRepository.saveUser(user)
                    _authState.value = AuthState.Authenticated(firebaseUser.uid)
                } else {
                    _authState.value = AuthState.Error("Firebase user is null after sign in")
                }
            } catch (e: Exception) {
                Log.e("EyrisAuth", "Firebase auth with Google failed", e)
                _authState.value = AuthState.Error(e.message ?: "Auth failed")
            }
        }
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun setLoading() {
        _authState.value = AuthState.Loading
    }
}

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val userId: String) : AuthState
    data class Error(val message: String) : AuthState
}
