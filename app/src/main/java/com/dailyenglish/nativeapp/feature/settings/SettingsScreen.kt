package com.dailyenglish.nativeapp.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailyenglish.nativeapp.data.repository.AuthState
import com.dailyenglish.nativeapp.data.repository.SyncMode
import com.dailyenglish.nativeapp.data.repository.SyncStatus
import com.dailyenglish.nativeapp.feature.study.ExamSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onLoginRequested: () -> Unit,
  viewModel: SettingsViewModel = hiltViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var dailyGoal by rememberSaveable { mutableStateOf(state.settings.dailyGoal.toString()) }
  var sentenceGoal by rememberSaveable { mutableStateOf(state.settings.sentenceGoal.toString()) }
  var focusMinutes by rememberSaveable { mutableStateOf(state.settings.focusMinutes.toString()) }
  var exam by rememberSaveable { mutableStateOf(state.settings.exam) }

  LaunchedEffect(state.settings) {
    dailyGoal = state.settings.dailyGoal.toString()
    sentenceGoal = state.settings.sentenceGoal.toString()
    focusMinutes = state.settings.focusMinutes.toString()
    exam = state.settings.exam
  }

  LazyColumn(
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("学习设置", style = MaterialTheme.typography.headlineLarge)
        Text("按你的节奏学习，进度留在自己手里。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    item { ExamSwitch(exam, { exam = it }) }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("每日目标", style = MaterialTheme.typography.titleMedium)
          OutlinedTextField(
            value = dailyGoal,
            onValueChange = { dailyGoal = it.filter(Char::isDigit) },
            label = { Text("每日新词目标") },
            supportingText = { Text("1–100 词") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = sentenceGoal,
            onValueChange = { sentenceGoal = it.filter(Char::isDigit) },
            label = { Text("每日翻译目标") },
            supportingText = { Text("1–20 句") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = focusMinutes,
            onValueChange = { focusMinutes = it.filter(Char::isDigit) },
            label = { Text("单次专注时长") },
            supportingText = { Text("1–120 分钟 · 下次计时生效") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
          )
          Button(
            onClick = {
              viewModel.save(
                dailyGoal = dailyGoal.toIntOrNull() ?: state.settings.dailyGoal,
                sentenceGoal = sentenceGoal.toIntOrNull() ?: state.settings.sentenceGoal,
                focusMinutes = focusMinutes.toIntOrNull() ?: state.settings.focusMinutes,
                exam = exam,
              )
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
          ) { Text(if (state.busy) "保存中…" else "保存学习计划") }
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(if (state.auth is AuthState.Member) "账号与同步" else "把进度带到任何设备", style = MaterialTheme.typography.titleMedium)
          when (val auth = state.auth) {
            is AuthState.Member -> {
              Text(auth.email, style = MaterialTheme.typography.titleSmall)
              Text(syncLabel(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              if (state.sync.problem.isNotBlank()) {
                Text(state.sync.problem, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
              }
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = viewModel::syncNow) { Text("立即同步") }
                OutlinedButton(onClick = viewModel::logout) { Text("退出登录") }
              }
            }
            is AuthState.Anonymous -> {
              Text("注册后，学习进度保存在你自己的账号里，换设备也能接着学。不登录也可以继续，进度只留在这台设备。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Button(onClick = onLoginRequested) { Text("登录或注册") }
            }
          }
        }
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("数据说明", style = MaterialTheme.typography.titleMedium)
          Text("学习进度保存在本机 SQLite；登录后使用与网页版一致的 revision 合并协议。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("备份导出、修改密码和完整统计将在后续阶段提供。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
    if (state.message.isNotBlank()) {
      item { Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
    }
    item {
      Text("7,291 个大纲词汇 · 253 句真题句子 · 44 套考研真题", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

private fun syncLabel(state: SettingsUiState): String = when (state.sync.status) {
  SyncStatus.IDLE -> if (state.sync.mode == SyncMode.MEMBER) "已同步" else "未登录"
  SyncStatus.SAVING -> "正在保存…"
  SyncStatus.DIRTY -> "有未同步的修改"
  SyncStatus.OFFLINE -> "离线，稍后自动重试"
  SyncStatus.SIGNED_OUT -> "登录已过期，进度仍在本机"
}
