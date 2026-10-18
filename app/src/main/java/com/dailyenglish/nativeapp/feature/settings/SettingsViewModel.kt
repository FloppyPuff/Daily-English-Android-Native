package com.dailyenglish.nativeapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyenglish.nativeapp.core.model.Settings
import com.dailyenglish.nativeapp.data.repository.AuthRepository
import com.dailyenglish.nativeapp.data.repository.AuthState
import com.dailyenglish.nativeapp.data.repository.ProgressRepository
import com.dailyenglish.nativeapp.data.repository.SyncManager
import com.dailyenglish.nativeapp.data.repository.SyncStatus
import com.dailyenglish.nativeapp.data.repository.SyncUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
  val settings: Settings = Settings(),
  val sync: SyncUiState = SyncUiState(),
  val auth: AuthState = AuthState.Anonymous(guest = false),
  val busy: Boolean = false,
  val message: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val progress: ProgressRepository,
  private val sync: SyncManager,
  private val auth: AuthRepository,
) : ViewModel() {
  private val _state = MutableStateFlow(SettingsUiState())
  val state: StateFlow<SettingsUiState> = _state.asStateFlow()

  init {
    viewModelScope.launch {
      progress.state.collect { value ->
        _state.update { it.copy(settings = value.settings) }
      }
    }
    viewModelScope.launch {
      sync.uiState.collect { value ->
        _state.update { it.copy(sync = value) }
      }
    }
    viewModelScope.launch {
      auth.state.collect { value ->
        _state.update { it.copy(auth = value) }
      }
    }
  }

  fun save(dailyGoal: Int, sentenceGoal: Int, focusMinutes: Int, exam: String) {
    if (_state.value.busy) return
    viewModelScope.launch {
      _state.update { it.copy(busy = true, message = "") }
      runCatching { progress.updateSettings(dailyGoal, sentenceGoal, focusMinutes, exam) }
        .onSuccess { _state.update { it.copy(busy = false, message = "学习计划已保存。") } }
        .onFailure { _state.update { it.copy(busy = false, message = "保存失败，请稍后再试。") } }
    }
  }

  fun syncNow() {
    viewModelScope.launch {
      sync.syncNow()
      val status = _state.value.sync.status
      _state.update {
        it.copy(
          message = when (status) {
            SyncStatus.OFFLINE -> "仍然离线，进度已留在本机。"
            SyncStatus.SIGNED_OUT -> "登录已过期，请重新登录。"
            else -> "已与账号同步。"
          },
        )
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      auth.logout()
      _state.update { it.copy(message = "已退出登录，本机进度仍然保留。") }
    }
  }
}
