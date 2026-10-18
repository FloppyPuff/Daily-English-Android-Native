# Daily English Native M3（完整对等）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 补齐笔记、完整统计/热力图、备份导出导入、修改密码、大屏 NavigationRail/双栏、动态取色，交付 1.0.0。

**Architecture:** 沿用单 app 模块与 version 3 状态。新增 `NotebookEngine`/`BackupCodec` 纯逻辑，新增 `feature/notebook`、`feature/stats` 页面与两个 ViewModel；`MainNavigation` 按宽度切换底部导航与 NavigationRail；动态取色保存在设备级 DataStore，不进入同步状态。

**Spec:** `docs/superpowers/specs/2026-10-18-native-android-design.md` §8、§9 M3  
**Tech:** Kotlin、Compose、Navigation 3、Hilt、Room、DataStore、SAF（Activity Result API）。

## Global Constraints

- applicationId `com.dailyenglish.native`；version `1.0.0`（versionCode 3）。
- 不修改状态 JSON 结构；备份导出/导入使用现有 version 3 payload。
- 备份文件上限 5 MB；导入前校验 `StateValidator` 与当前词句 ID；导入覆盖必须二次确认。
- 动态取色仅设备本地偏好，不写入 `AppState`、不参与同步。
- 笔记规则与网页一致：收藏词/句；错题词 = `difficult` 或真题 `words`；薄弱句 = `rating == "review"`。
- 统计与网页一致：当前考试方向、去重事件、focus 事件不计入热力图、focus ≥60 秒算活跃日。
- 大屏：`>=600dp` 使用 NavigationRail；笔记/记录在 `>=720dp` 使用双栏。
- 修改密码成功后保留当前 token；其他设备会话失效由服务端处理。

## Tasks

### Task 1: 统计热力图与错题规则

**Files:** `core/learning/LearningEngine.kt`, `core/learning/LearningEngineTest.kt`

**Interfaces:**
- Produces: `data class HeatCell(val day: String, val count: Int, val level: Int)`；`Statistics.heatmap: List<HeatCell>`。
- Produces: `LearningEngine.buildQueue(..., "mistakes")` 同时包含 `state.papers[*].words`。

- [x] **Step 1: 写失败测试**

```kotlin
@Test fun heatmapKeepsTwelveWeeksOfUniqueExamEvents() {
  val day = localDay(at)
  val state = createState().copy(events = listOf(
    StudyEvent("word", "abandon", at = at, exam = "cet6"),
    StudyEvent("word", "abandon", at = at + 1, exam = "cet6"),
    StudyEvent("sentence", "s001", at = at + 2, exam = "cet6"),
    StudyEvent("focus", seconds = 600, at = at, exam = "cet6"),
    StudyEvent("word", "abandon", at = at, exam = "postgrad"),
  ))
  val stats = LearningEngine.statistics(state, listOf(word("abandon")), at)
  assertEquals(84, stats.heatmap.size)
  assertEquals(day, stats.heatmap.last().day)
  assertEquals(2, stats.heatmap.last().count)
  assertEquals(1, stats.heatmap.last().level)
}
```

- [x] **Step 2: 运行失败**：`./gradlew :app:testDebugUnitTest --tests '*LearningEngineTest*'`，预期 `Unresolved reference: heatmap`。
- [x] **Step 3: 实现**：按 `addDays(at, -83..0)` 生成 84 天；只取当前 exam 且 `type != "focus"` 的事件，按 `${type}:${id}` 去重；level `>20→3, >5→2, >0→1`。`mistakes` 队列并入真题词。
- [x] **Step 4: 运行通过**；提交 `feat: add stats heatmap and paper mistake words`。

### Task 2: 笔记领域与页面

**Files:** Create `core/learning/NotebookEngine.kt`, `feature/notebook/NotebookViewModel.kt`, `feature/notebook/NotebookScreen.kt`; Modify `ui/navigation/MainScaffold.kt`, `ui/AppRoot.kt`; Test `core/learning/NotebookEngineTest.kt`.

**Interfaces:**
- `NotebookEngine.collect(state: AppState, words: List<WordDto>, sentences: List<SentenceDto>, tab: String, query: String = ""): NotebookData`
- `NotebookData(words: List<NotebookWord>, sentences: List<NotebookSentence>)`；每项含原始 DTO 与标记。
- `NotebookScreen(ui, onTab, onQuery, onOpenWord, onOpenSentence, onPracticeWords, onPracticeSentences)`。
- `StudyViewModel.selectSentence(id)` 已存在；打开句子前调用它。

- [x] **Step 1: 失败测试**：收藏词只出现在 saved；`difficult` 词和 `papers[*].words` 词出现在 mistakes；`review` 句出现在 mistakes；搜索匹配词头与释义；非当前考试方向被过滤。
- [x] **Step 2: 运行失败**。
- [x] **Step 3: 实现** `NotebookEngine` 与测试通过；列表最多各 60 条（与网页一致）。
- [x] **Step 4: 实现 ViewModel**：加载 content、`progress.load()`，收集 state 后重算；`tab`、`query` 状态；错误可重试。
- [x] **Step 5: 实现 Screen**：收藏/错题分段；搜索框；单词/句子卡片；空态；`已收藏/薄弱/真题` 标签；点击词进入 `Words(wordId)`，点击句调用 `selectSentence` 后进入 `Sentences`；练习按钮分别 `startSession("saved"/"mistakes")` 和打开首句。
- [x] **Step 6: 路由**：替换 `Notebook` 占位；`Notebook` 选中态保持；宽屏用双栏（`>=720dp` 时 Row 两列，列表各 60 条）。
- [x] **Step 7: 构建并提交** `feat: add notebook with saved and weak items`。

### Task 3: 记录页

**Files:** Create `feature/stats/StatsViewModel.kt`, `feature/stats/StatsScreen.kt`; Modify `ui/navigation/MainScaffold.kt`; Test `feature/stats` 逻辑复用 Task 1.

**Interfaces:**
- `StatsUiState(loading, error, stats: Statistics?, exam: String)`。
- `StatsViewModel.setExam(exam)` 复用 `ProgressRepository.updateSettings`。
- `StatsScreen(ui, onExam)`。

- [x] **Step 1: 实现 ViewModel**：加载内容与进度，收集 progress 重算 `LearningEngine.statistics`；`retry`。
- [x] **Step 2: 实现 UI**：页头 + 考试切换；5 张统计卡（累计已学/较熟悉/累计学习天数/累计专注分钟/真题）；7 日词汇柱状图；掌握比例进度条与图例；12 周热力图（84 格，4 档颜色 + 图例）。
- [x] **Step 3: 响应式**：`>=720dp` 统计卡 3 列、图表双栏；手机 2 列单栏。
- [x] **Step 4: 构建并提交** `feat: add stats dashboard and heatmap`。

### Task 4: 备份导出/导入

**Files:** Create `core/sync/BackupCodec.kt`; Modify `data/repository/ProgressRepository.kt`, `feature/settings/SettingsScreen.kt`, `feature/settings/SettingsViewModel.kt`; Test `core/sync/BackupCodecTest.kt`, `androidTest/.../ProgressRepositoryTest.kt`.

**Interfaces:**
- `BackupCodec.encode(state): String`（pretty JSON）。
- `BackupCodec.decode(text: String, knownWords: Set<String>, knownSentences: Set<String>): BackupResult`；`Ok(state)` / `Error(message)`；5 MB 上限。
- `ProgressRepository.importState(state: AppState)`：`mapper.replaceAll` + 更新内存 + `markDirty`。

- [x] **Step 1: 失败测试**：合法 v3 往返；版本/设置无效被拒；超过 5 MB 被拒；未知词句 ID 被拒；`normalizeLegacy` 生效。
- [x] **Step 2: 运行失败**；实现 `BackupCodec`；测试通过。
- [x] **Step 3: 设备测试**：导入替换 Room，重载后只保留导入数据且 `markDirty` 触发。
- [x] **Step 4: Settings UI**：创建文档 launcher（`application/json`，文件名 `daily-english-YYYY-MM-DD.json`）；打开文档 launcher；读取上限 5 MB；确认对话框后 `importState`；失败提示“原有数据未改变”。
- [x] **Step 5: 构建并提交** `feat: add backup export and restore`。

### Task 5: 修改密码

**Files:** Create `core/security/PasswordRules.kt`; Modify `feature/auth/AuthViewModel.kt`, `feature/settings/SettingsViewModel.kt`, `feature/settings/SettingsScreen.kt`; Test `core/security/PasswordRulesTest.kt`.

**Interfaces:**
- `PasswordRules.problem(password: String): String?`：<8、>128、缺字母或数字。
- `SettingsViewModel.changePassword(current, next)`；成功后 message“密码已修改，其他设备需要重新登录”。

- [x] **Step 1: 失败测试**：7 位、纯数字、合法密码；Auth 注册复用同一规则。
- [x] **Step 2: 实现并测试通过**。
- [x] **Step 3: UI**：仅 Member 显示当前/新密码输入（密码遮罩）与按钮；busy 禁用；错误红色显示。
- [x] **Step 4: 构建并提交** `feat: allow password change from settings`。

### Task 6: 大屏导航与动态取色

**Files:** Modify `ui/navigation/MainScaffold.kt`, `ui/AppRoot.kt`, `MainActivity.kt`, `feature/settings/SettingsViewModel.kt`, `feature/settings/SettingsScreen.kt`; Create `data/local/PreferenceStore.kt`; Modify `di/SessionModule.kt` or new module.

**Interfaces:**
- `PreferenceStore.dynamicColor: Flow<Boolean>`、`setDynamicColor(Boolean)`；DataStore 名 `preferences`。
- `AppUiState.dynamicColor: Boolean`；`AppViewModel.toggleDynamicColor()` 或在 SettingsVM 中设置。
- `SettingsUiState.dynamicColor: Boolean`。

- [x] **Step 1: 实现 PreferenceStore 并绑定 Hilt**。
- [x] **Step 2: MainActivity** 在主题外层收集 `dynamicColor`，传入 `DailyEnglishTheme(dynamicColor = ...)`；默认关闭。
- [x] **Step 3: MainNavigation**：`BoxWithConstraints`，`maxWidth >= 600.dp` 用 `Row + NavigationRail`，否则底部 NavigationBar；选中/返回逻辑不变。
- [x] **Step 4: Settings** 增加“外观”卡片 + Switch，Android 12 以下禁用并说明。
- [x] **Step 5: 构建；旋转到横屏验证 NavigationRail；提交** `feat: add adaptive navigation and dynamic color`。

### Task 7: 1.0.0 收尾与验证

- [x] 版本 `1.0.0`（versionCode 3）。
- [x] JVM 单测与 `connectedDebugAndroidTest` 全绿。
- [x] 真机：笔记收藏/错题/搜索；记录页数字、7 日图、热力图；导出备份生成文件；导入同一备份并恢复；修改密码表单校验；横屏 NavigationRail；深色模式。
- [x] 更新 README/PROGRESS，记录真实账号登录与跨端同步已由用户验证。
- [x] 构建 APK、复制桌面、SHA-256；提交并 tag `v1.0.0-m3`。

## Self-Review

- Spec coverage：笔记、统计/热力图、专注分钟、备份、改密、NavigationRail/双栏、动态取色均有任务；整体视觉打磨贯穿页面。
- 无占位：接口、文件、行为与测试均已命名。
- 类型一致：`Statistics.heatmap` 只被 Task 3 使用；`AppState` 不变；`BackupCodec` 与 `ProgressRepository.importState` 签名一致。
