package Com.elsewhere.eyris.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Com.elsewhere.eyris.data.repositories.UserRepository
import Com.elsewhere.eyris.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuth()
    }

    private fun checkAuth() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signInAnonymously() { // For demo purposes, or user can implement Google Sign In
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInAnonymously().await() // This needs tasks-ktx
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = User(
                        userId = firebaseUser.uid,
                        displayName = "Guest User",
                        email = "guest@example.com"
                    )
                    userRepository.saveUser(user)
                    _authState.value = AuthState.Authenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Auth failed")
            }
        }
    }
    
    // In a real app, we'd have Google Sign In here.
    // For this applet, we'll simulate the "Google Sign In" button to just sign in anonymously or with a mock.
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
