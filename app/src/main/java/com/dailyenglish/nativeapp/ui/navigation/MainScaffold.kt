package com.dailyenglish.nativeapp.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dailyenglish.nativeapp.theme.NumberStyle
import com.dailyenglish.nativeapp.theme.WordHeadwordStyle

private data class TopDestination(
  val key: NavKey,
  val label: String,
  val icon: ImageVector,
)

private val topDestinations = listOf(
  TopDestination(Today, "今日", Icons.Outlined.Home),
  TopDestination(Practice, "练习", Icons.Outlined.MenuBook),
  TopDestination(Notebook, "笔记", Icons.Outlined.BookmarkBorder),
  TopDestination(Stats, "记录", Icons.Outlined.BarChart),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Today)
  val current = backStack.lastOrNull()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(titleFor(current), style = MaterialTheme.typography.titleLarge)
            Text("A LITTLE, EVERY DAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        navigationIcon = {
          if (backStack.size > 1) {
            IconButton(onClick = { backStack.removeLastOrNull() }) {
              Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
            }
          }
        },
        actions = {
          IconButton(onClick = { backStack.add(Settings) }) {
            Icon(Icons.Outlined.Settings, contentDescription = "学习设置")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    bottomBar = {
      NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        topDestinations.forEach { destination ->
          NavigationBarItem(
            selected = isSelected(current, destination.key),
            onClick = {
              if (backStack.lastOrNull() != destination.key) {
                backStack.clear()
                backStack.add(destination.key)
              }
            },
            icon = { Icon(destination.icon, contentDescription = null) },
            label = { Text(destination.label) },
          )
        }
      }
    },
  ) { innerPadding ->
    Box(Modifier.padding(innerPadding)) {
      NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
          entry<Today> {
            TodayScreen(
              onStart = { backStack.add(Practice) },
              onReview = { backStack.add(Words("review")) },
            )
          }
          entry<Practice> {
            PracticeScreen(
              onWords = { backStack.add(Words("new")) },
              onSentences = { backStack.add(Sentences) },
              onReview = { backStack.add(Review) },
            )
          }
          entry<Words> { key ->
            WordsScreen(mode = key.mode, wordId = key.wordId)
          }
          entry<Sentences> { SentencesScreen() }
          entry<Review> { ReviewScreen(onStart = { backStack.add(Words("review")) }) }
          entry<Notebook> {
            ComingSoonScreen(
              icon = Icons.Outlined.BookmarkBorder,
              eyebrow = "SECOND STAGE",
              title = "我的笔记，马上就好。",
              body = "收藏词和收藏句会在 M3 阶段回到这里。现在可以先在词卡和句子里收藏，数据不会丢。",
            )
          }
          entry<Stats> {
            ComingSoonScreen(
              icon = Icons.Outlined.BarChart,
              eyebrow = "THIRD STAGE",
              title = "把积累，看得见。",
              body = "学习记录、七日节奏和 12 周热力图会在 M3 阶段补上。M1 先保证每一次练习都被真实记录。",
            )
          }
          entry<Settings> {
            SettingsScreen()
          }
        },
      )
    }
  }
}

private fun isSelected(current: NavKey?, target: NavKey): Boolean = when (target) {
  Today -> current is Today
  Practice -> current is Practice || current is Words || current is Sentences || current is Review
  Notebook -> current is Notebook
  Stats -> current is Stats
  else -> false
}

private fun titleFor(key: NavKey?): String = when (key) {
  Today -> "今日学习"
  Practice -> "自由练习"
  is Words -> "词汇练习"
  Sentences -> "句子翻译"
  Review -> "复习计划"
  Notebook -> "我的笔记"
  Stats -> "学习记录"
  Settings -> "学习设置"
  else -> "今日学习"
}

@Composable
private fun TodayScreen(onStart: () -> Unit, onReview: () -> Unit) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          "把英语，变成日常。",
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          "沿着今天的计划，一次只专注一件事。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("今日计划", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.weight(1f))
            AssistChip(onClick = onStart, label = { Text("开始") })
          }
          PlanRow("01", "复习旧词", "先温习，再向前。", onReview)
          HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
          PlanRow("02", "学习新词", "一次只记住一个词。", onStart)
          HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
          PlanRow("03", "翻译句子", "读懂结构，再写下理解。", onStart)
          Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("进入今日学习")
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
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
          }
          Spacer(Modifier.width(14.dp))
          Column(Modifier.weight(1f)) {
            Text("先学一点，再回来。", style = MaterialTheme.typography.titleMedium)
            Text("词卡、复习和句子都从这里开始。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
    item {
      Text(
        "连续学习 0 天 · 今日专注 0 分钟",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
      )
    }
  }
}

@Composable
private fun PlanRow(index: String, title: String, caption: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(index, style = NumberStyle.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize), color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleMedium)
      Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    FilledTonalButton(onClick = onClick) { Text("进入") }
  }
}

@Composable
private fun PracticeScreen(onWords: () -> Unit, onSentences: () -> Unit, onReview: () -> Unit) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("自由练习", style = MaterialTheme.typography.headlineLarge)
        Text("想多学一点，或专门巩固某一项，都从这里开始。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    item { PracticeCard("词汇练习", "闪卡、拼写或浏览词库", Icons.Outlined.MenuBook, onWords) }
    item { PracticeCard("句子翻译", "读懂结构，再写下你的理解", Icons.Outlined.Translate, onSentences) }
    item { PracticeCard("复习计划", "按到期时间，先温习再向前", Icons.Outlined.Repeat, onReview) }
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
            Text("真题练习", style = MaterialTheme.typography.titleMedium)
            Text("44 套考研真题，下一阶段开放", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          AssistChip(onClick = {}, label = { Text("M2") }, enabled = false)
        }
      }
    }
  }
}

@Composable
private fun PracticeCard(title: String, caption: String, icon: ImageVector, onClick: () -> Unit) {
  Card(
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
      Spacer(Modifier.width(16.dp))
      Column(Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun WordsScreen(mode: String, wordId: String?) {
  ComingSoonScreen(
    icon = Icons.Outlined.MenuBook,
    eyebrow = "WORDS",
    title = "词卡正在装订。",
    body = if (wordId != null) "词库条目 $wordId 会在下一步接入。" else "当前模式：$mode。M1 的闪卡、拼写和词库会在下一个任务接入。",
  )
}

@Composable
private fun SentencesScreen() {
  ComingSoonScreen(
    icon = Icons.Outlined.Translate,
    eyebrow = "TRANSLATION",
    title = "句子练习即将接入。",
    body = "253 句真题长难句和翻译段落已经内置，下一步接入输入、参考与自评。",
  )
}

@Composable
private fun ReviewScreen(onStart: () -> Unit) {
  ComingSoonScreen(
    icon = Icons.Outlined.Repeat,
    eyebrow = "REVIEW",
    title = "复习计划即将接入。",
    body = "到期词、后续安排和复习会话会在词卡完成后接入。",
    action = "先开始词汇练习" to onStart,
  )
}

@Composable
private fun SettingsScreen() {
  ComingSoonScreen(
    icon = Icons.Outlined.Settings,
    eyebrow = "SETTINGS",
    title = "学习设置即将接入。",
    body = "每日目标、考试方向、账号与同步状态会在数据层完成后接入。",
  )
}

@Composable
private fun ComingSoonScreen(
  icon: ImageVector,
  eyebrow: String,
  title: String,
  body: String,
  action: Pair<String, () -> Unit>? = null,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 40.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
      Box(contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
      }
    }
    Text(eyebrow, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    Text(title, style = MaterialTheme.typography.headlineMedium)
    Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (action != null) {
      Button(onClick = action.second) { Text(action.first) }
    }
  }
}
