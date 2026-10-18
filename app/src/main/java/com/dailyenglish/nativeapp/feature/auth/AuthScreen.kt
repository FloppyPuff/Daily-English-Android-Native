package com.dailyenglish.nativeapp.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
  state: AuthUiState,
  onMode: (AuthMode) -> Unit,
  onEmail: (String) -> Unit,
  onPassword: (String) -> Unit,
  onSubmit: () -> Unit,
  onGuest: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .imePadding()
      .statusBarsPadding()
      .padding(horizontal = 24.dp, vertical = 36.dp),
    verticalArrangement = Arrangement.spacedBy(22.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
        Icon(
          Icons.Outlined.MenuBook,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.padding(12.dp),
        )
      }
      Column {
        Text("Daily English.", style = MaterialTheme.typography.headlineMedium)
        Text("一点积累，每日发生", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = MaterialTheme.shapes.extraLarge,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("A QUIET PLACE TO BEGIN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text("从今天开始，把英语变成日常。", style = MaterialTheme.typography.headlineMedium)
        Text(
          "创建账号后，这台设备上已有的进度会自动上传；之后换设备，也能接着学。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
          SegmentedButton(
            selected = state.mode == AuthMode.REGISTER,
            onClick = { onMode(AuthMode.REGISTER) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
          ) { Text("注册") }
          SegmentedButton(
            selected = state.mode == AuthMode.LOGIN,
            onClick = { onMode(AuthMode.LOGIN) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
          ) { Text("登录") }
        }

        OutlinedTextField(
          value = state.email,
          onValueChange = onEmail,
          label = { Text("邮箱") },
          placeholder = { Text("you@example.com") },
          singleLine = true,
          enabled = !state.busy,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = state.password,
          onValueChange = onPassword,
          label = { Text("密码") },
          supportingText = { Text("至少 8 位，含字母与数字") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          enabled = !state.busy,
          modifier = Modifier.fillMaxWidth(),
        )
        if (state.error.isNotBlank()) {
          Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onSubmit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
          if (state.busy) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.size(8.dp))
          }
          Text(if (state.mode == AuthMode.REGISTER) "创建账号并开始" else "登录并继续")
        }
        TextButton(onClick = onGuest, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
          Text("暂不登录，先在本机学习")
        }
      }
    }
    Text(
      "每天一点，不急着抵达。",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
