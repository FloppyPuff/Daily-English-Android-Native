package com.dailyenglish.nativeapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyenglish.nativeapp.data.repository.AuthException
import com.dailyenglish.nativeapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { REGISTER, LOGIN }

data class AuthUiState(
  val mode: AuthMode = AuthMode.REGISTER,
  val email: String = "",
  val password: String = "",
  val busy: Boolean = false,
  val error: String = "",
)

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val auth: AuthRepository,
) : ViewModel() {
  private val _state = MutableStateFlow(AuthUiState())
  val state: StateFlow<AuthUiState> = _state.asStateFlow()

  fun setMode(mode: AuthMode) {
    _state.update { it.copy(mode = mode, error = "") }
  }

  fun updateEmail(value: String) {
    _state.update { it.copy(email = value, error = "") }
  }

  fun updatePassword(value: String) {
    _state.update { it.copy(password = value, error = "") }
  }

  fun submit() {
    val current = _state.value
    if (current.busy) return
    val email = current.email.trim()
    val problem = when {
      !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email) -> "请输入有效的邮箱地址"
      current.password.length < 8 -> "密码至少 8 位"
      !current.password.any { it.isLetter() } || !current.password.any { it.isDigit() } -> "密码需同时包含字母和数字"
      else -> ""
    }
    if (problem.isNotEmpty()) {
      _state.update { it.copy(error = problem) }
      return
    }
    viewModelScope.launch {
      _state.update { it.copy(busy = true, error = "") }
      try {
        if (current.mode == AuthMode.REGISTER) auth.register(email, current.password)
        else auth.login(email, current.password)
        _state.update { it.copy(busy = false, password = "") }
      } catch (error: AuthException) {
        _state.update { it.copy(busy = false, error = error.message ?: "暂时无法完成，请稍后再试") }
      } catch (error: Exception) {
        _state.update { it.copy(busy = false, error = "暂时无法完成，请稍后再试") }
      }
    }
  }
}
