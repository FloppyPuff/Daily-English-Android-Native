package com.dailyenglish.nativeapp.feature.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailyenglish.nativeapp.theme.NumberStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSheet(
  ui: StudyUiState,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
      Text("给学习一段完整的时间。", style = MaterialTheme.typography.titleLarge)
      Text(
        formatClock(ui.focus.remainingSeconds),
        style = NumberStyle.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize),
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text("专注用时按实际经过的时间记录，暂停不计时。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = if (ui.focus.running) onPause else onStart,
          modifier = Modifier.weight(1f),
        ) { Text(if (ui.focus.running) "暂停专注" else "开始专注") }
        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("重新计时") }
      }
      Spacer(Modifier.height(4.dp))
      Text("关闭应用会结束本次计时，与网页版一致。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

fun formatClock(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
