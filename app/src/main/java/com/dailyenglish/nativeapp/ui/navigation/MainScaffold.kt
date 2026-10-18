package com.dailyenglish.nativeapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dailyenglish.nativeapp.feature.papers.PaperGroupScreen
import com.dailyenglish.nativeapp.feature.papers.PaperSetScreen
import com.dailyenglish.nativeapp.feature.papers.PapersScreen
import com.dailyenglish.nativeapp.feature.papers.PapersViewModel
import com.dailyenglish.nativeapp.feature.settings.SettingsScreen
import com.dailyenglish.nativeapp.feature.study.ComingSoonScreen
import com.dailyenglish.nativeapp.feature.study.FocusSheet
import com.dailyenglish.nativeapp.feature.study.PracticeScreen
import com.dailyenglish.nativeapp.feature.study.ReviewScreen
import com.dailyenglish.nativeapp.feature.study.SentencesScreen
import com.dailyenglish.nativeapp.feature.study.StudyViewModel
import com.dailyenglish.nativeapp.feature.study.TodayScreen
import com.dailyenglish.nativeapp.feature.study.WordsScreen

private data class TopDestination(val key: NavKey, val label: String, val icon: ImageVector)

private val topDestinations = listOf(
  TopDestination(Today, "今日", Icons.Outlined.Home),
  TopDestination(Practice, "练习", Icons.Outlined.MenuBook),
  TopDestination(Notebook, "笔记", Icons.Outlined.BookmarkBorder),
  TopDestination(Stats, "记录", Icons.Outlined.BarChart),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
  studyViewModel: StudyViewModel,
  papersViewModel: PapersViewModel,
  snackbarHostState: SnackbarHostState,
  onLoginRequested: () -> Unit,
) {
  val ui by studyViewModel.state.collectAsStateWithLifecycle()
  val papersUi by papersViewModel.state.collectAsStateWithLifecycle()
  LaunchedEffect(papersViewModel) {
    papersViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
  }
  val backStack = rememberNavBackStack(Today)
  val current = backStack.lastOrNull()
  var showFocus by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
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
          TextButton(onClick = { showFocus = true }) {
            Icon(Icons.Outlined.Timer, contentDescription = null)
            Text(
              if (ui.focus.running) formatClock(ui.focus.remainingSeconds) else "专注",
              modifier = Modifier.padding(start = 6.dp),
            )
          }
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
              ui = ui,
              onExam = studyViewModel::setExam,
              onStartWords = { backStack.add(Words("new")) },
              onReview = { backStack.add(Words("review")) },
              onSentences = { backStack.add(Sentences) },
            )
          }
          entry<Practice> {
            PracticeScreen(
              ui = ui,
              onExam = studyViewModel::setExam,
              onWords = { backStack.add(Words("new")) },
              onSentences = { backStack.add(Sentences) },
              onReview = { backStack.add(Review) },
              onPapers = { backStack.add(Papers) },
            )
          }
          entry<Papers> {
            PapersScreen(ui = papersUi, progress = ui.progress, onOpenPaper = { backStack.add(PaperSet(it)) })
          }
          entry<PaperSet> { key ->
            PaperSetScreen(
              ui = papersUi,
              progress = ui.progress,
              setId = key.id,
              onLoad = papersViewModel::openPaper,
              onOpenGroup = { backStack.add(PaperGroup(key.id, it)) },
            )
          }
          entry<PaperGroup> { key ->
            PaperGroupScreen(
              ui = papersUi,
              setId = key.setId,
              groupIndex = key.index,
              onLoad = papersViewModel::openGroup,
              onPick = papersViewModel::pick,
              onAnswer = papersViewModel::updateAnswer,
              onCheck = papersViewModel::toggleReference,
              onSubmit = papersViewModel::submit,
              onRate = papersViewModel::rate,
              onReset = papersViewModel::reset,
            )
          }
          entry<Words> { key ->
            WordsScreen(
              ui = ui,
              mode = key.mode,
              wordId = key.wordId,
              onStartSession = studyViewModel::startSession,
              onReveal = studyViewModel::revealWord,
              onRate = studyViewModel::rateWord,
              onNext = studyViewModel::nextWord,
              onSpellingInput = studyViewModel::setSpellingInput,
              onCheckSpelling = studyViewModel::checkSpelling,
              onToggleSave = studyViewModel::toggleSavedWord,
              onSpeak = studyViewModel::speakWord,
              onNewSession = studyViewModel::newWordSession,
            )
          }
          entry<Review> {
            ReviewScreen(ui = ui, onStart = { backStack.add(Words("review")) })
          }
          entry<Sentences> {
            SentencesScreen(
              ui = ui,
              onEnsure = studyViewModel::ensureSentence,
              onSelect = studyViewModel::selectSentence,
              onDraft = studyViewModel::updateSentenceDraft,
              onReveal = studyViewModel::revealSentence,
              onRate = studyViewModel::rateSentence,
              onNext = studyViewModel::nextSentence,
              onSave = studyViewModel::toggleSavedSentence,
              onSpeak = studyViewModel::speakSentence,
            )
          }
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
            SettingsScreen(onLoginRequested = onLoginRequested)
          }
        },
      )
    }
  }

  if (showFocus) {
    FocusSheet(
      ui = ui,
      onStart = studyViewModel::startFocus,
      onPause = studyViewModel::pauseFocus,
      onReset = studyViewModel::resetFocus,
      onDismiss = { showFocus = false },
    )
  }
}

private fun isSelected(current: NavKey?, target: NavKey): Boolean = when (target) {
  Today -> current is Today
  Practice -> current is Practice || current is Words || current is Sentences || current is Review || current is Papers || current is PaperSet || current is PaperGroup
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
  Papers -> "真题练习"
  is PaperSet -> "试卷"
  is PaperGroup -> "真题练习"
  Notebook -> "我的笔记"
  Stats -> "学习记录"
  Settings -> "学习设置"
  else -> "今日学习"
}

private fun formatClock(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
