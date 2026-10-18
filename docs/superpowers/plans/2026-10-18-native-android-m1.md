# Daily English Native M1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first installable native Android APK of Daily English (M1): auth/guest, today plan, words, review, sentences, TTS, sync, settings.

**Architecture:** Single Compose app module with Hilt, Room, DataStore, Retrofit/OkHttp and kotlinx.serialization. Room is local truth; a mapper produces the existing version-3 state JSON for the web-compatible `/api/state` sync. Content is bundled from `web/data` into assets at build time.

**Tech Stack:** Kotlin 2.x, Jetpack Compose, Material 3, Navigation Compose, ViewModel/StateFlow, Hilt, Room, DataStore, Retrofit/OkHttp, kotlinx.serialization, WorkManager, TextToSpeech.

**Spec:** `docs/superpowers/specs/2026-10-18-native-android-design.md`

## Global Constraints

- Project: `native-android/`; applicationId `com.dailyenglish.native`; Kotlin namespace/package `com.dailyenglish.nativeapp` (`native` is a Java keyword); app label `Daily English Native`; version `0.1.0` (versionCode 1).
- minSdk 24; compileSdk/targetSdk 36; JDK 21 (Android Studio JBR).
- API base `https://101.37.23.76:10444/`; every mobile request sends `x-client: daily-english-mobile`; member requests send `Authorization: Bearer <token>`.
- State contract is version 3, exactly as in spec §6.2. Never add native-only fields to the synced payload.
- Content counts: 7,291 words, 253 sentences, 44 paper sets. Copy from `web/data`; do not edit content by hand.
- Use Material 3; no WebView; brand sage/paper palette; light/dark; 48dp touch targets.
- Token at rest: Android Keystore AES-GCM, stored in DataStore. Never log token/password/backup.
- No backend changes. No papers/notebook/stats detail in M1; show clear next-phase empty states.
- Run `gradlew.bat testDebugUnitTest` and `gradlew.bat assembleDebug` before claiming completion.

## File Map

| Area | Files |
|---|---|
| Gradle | `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| App entry | `DailyEnglishApplication.kt`, `MainActivity.kt`, `ui/AppRoot.kt` |
| Theme | `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` |
| Navigation | `ui/navigation/AppDestination.kt`, `AppNavHost.kt`, `MainScaffold.kt` |
| Core model | `core/model/State.kt`, `Content.kt`, `Session.kt` |
| Learning | `core/learning/LearningEngine.kt`, `StateValidator.kt` |
| Sync | `core/sync/StateMapper.kt`, `MergeStates.kt` |
| Content data | `data/content/ContentDto.kt`, `ContentRepository.kt` |
| Local | `data/local/Entities.kt`, `Daos.kt`, `DailyEnglishDatabase.kt`, `ProgressRepository.kt`, `TokenStore.kt`, `SessionPreferences.kt` |
| Remote | `data/remote/ApiService.kt`, `ApiDtos.kt`, `RemoteDataSource.kt`, `NetworkModule.kt` |
| Repositories | `data/repository/AuthRepository.kt`, `SyncManager.kt`, `SyncWorker.kt` |
| TTS | `data/tts/TtsManager.kt` |
| UI features | `feature/auth/*`, `feature/today/*`, `feature/words/*`, `feature/sentences/*`, `feature/review/*`, `feature/settings/*`, `feature/placeholder/*` |
| Tests | `app/src/test/java/com/dailyenglish/native/core/...`, `data/...`, `app/src/test/resources/state-v3.json` |

---

### Task 1: Scaffold, git, build

**Files:**
- Create: `native-android/` (official `empty-activity` template)
- Modify: `settings.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
- Create: `README.md`, `.gitignore`, `docs/superpowers/specs/2026-10-18-native-android-design.md`, `docs/superpowers/plans/2026-10-18-native-android-m1.md`

**Interfaces:**
- Produces: compilable Compose app with applicationId `com.dailyenglish.native`, version 0.1.0.

- [ ] Create project with the official CLI:
  `android-cli --sdk=D:\Software\Tool\AndroidStudioSDK create empty-activity --name="Daily English Native" --applicationId=com.dailyenglish.native --namespace=com.dailyenglish.native --minSdk=24 --output=native-android`
- [ ] Inspect `gradle/libs.versions.toml` and record AGP/Kotlin/Compose versions. Add Aliyun Google/Public Maven mirrors before `google()`/`mavenCentral()` in `pluginManagement` and `dependencyResolutionManagement`.
- [ ] Set `versionCode 1`, `versionName "0.1.0"`, app label; ensure `buildFeatures { compose = true; buildConfig = true }`.
- [ ] Copy spec and this plan into `native-android/docs/superpowers/...`; add README and `.gitignore` (ignore `.gradle/`, `build/`, `local.properties`, `*.apk`, `.idea/` except run configs).
- [ ] `git init` in `native-android/`; commit scaffold + docs.
- [ ] Run `gradlew.bat assembleDebug` with `JAVA_HOME=D:\Software\Tool\AndroidStudio\jbr` and verify APK exists.
- Expected: `app/build/outputs/apk/debug/app-debug.apk`.
- Commit: `chore: scaffold native Android project`.

### Task 2: Dependencies, theme, app shell

**Files:**
- Modify: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `settings.gradle.kts`
- Create: `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`, `ui/navigation/AppDestination.kt`, `MainScaffold.kt`, `ui/AppRoot.kt`
- Modify: `MainActivity.kt`, `AndroidManifest.xml`

**Interfaces:**
- Produces: `DailyEnglishTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)`, `AppDestination` top-level routes, placeholder screens.

- [ ] Add version-catalog entries for Hilt, Hilt Navigation Compose, Room (runtime/ktx/compiler), KSP, kotlinx.serialization plugin/runtime, Retrofit, OkHttp logging, WorkManager, Hilt Work, DataStore, lifecycle-viewmodel-compose, navigation-compose, activity-compose, core-splashscreen, test deps (JUnit4, MockWebServer, coroutines-test).
- [ ] Apply plugins: `com.google.dagger.hilt.android`, `com.google.devtools.ksp`, `org.jetbrains.kotlin.plugin.serialization`; add `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`.
- [ ] Implement the M3 color schemes from spec §8.1 (sage primary, paper surfaces, dark scheme); typography with serif display/headline and sans body; shapes 12/16/24.
- [ ] Implement `MainActivity` with `enableEdgeToEdge()` and `DailyEnglishTheme { AppRoot() }`.
- [ ] Implement `AppDestination` enum/sealed classes for Today, Practice, Notebook, Stats; `MainScaffold` with `NavigationBar`, placeholder content and a settings action in the top bar.
- [ ] Build and install; verify app opens with M3 shell.
- Commit: `feat: add Material 3 theme and navigation shell`.

### Task 3: Content pipeline and repository

**Files:**
- Create: `app/src/main/assets/content/` via Gradle task (no hand copy)
- Create: `data/content/ContentDto.kt`, `ContentRepository.kt`
- Modify: `app/build.gradle.kts`
- Test: `app/src/test/java/com/dailyenglish/native/data/content/ContentDtoTest.kt`

**Interfaces:**
- Produces:
  - `@Serializable data class WordDto(id, phonetic=null, pos=null, meaning, senses=emptyList(), exams, example=null, forms=emptyList(), hits=emptyMap(), tiers=emptyMap(), ...)`
  - `@Serializable data class SentenceDto(id, exams, kind, direction, level=null, topic=null, en, zh, analysis=emptyList(), keywords=emptyList(), source=null, ...)`
  - `class ContentRepository @Inject constructor(@ApplicationContext context: Context, json: Json)` with `suspend fun load(): Content` and `suspend fun paper(id: String): PaperDto?`
  - `data class Content(words: List<WordDto>, sentences: List<SentenceDto>, paperIndex: List<PaperIndexDto>)`

- [ ] Add a Gradle `SyncContent` task copying `rootProject.projectDir.parentFile.resolve("web/data")` to `app/src/main/assets/content`, wired to `preBuild`. Fail with a clear message if the source directory is missing.
- [ ] Define serializable DTOs with `ignoreUnknownKeys = true`; preserve all fields needed by UI (word example/forms/hits/tiers; sentence analysis/keywords/source).
- [ ] Implement `ContentRepository` to read `words.json`, `sentences.json`, `papers/index.json` on `Dispatchers.IO`; papers load lazily by `papers/<id>.json`.
- [ ] Test parsing the real assets in a JVM test by reading `src/main/assets/content/...` from the project directory; assert counts 7291/253/44 and sample fields (`abandon` has `example`, `s001` has analysis).
- [ ] Run `gradlew.bat testDebugUnitTest`; commit: `feat: bundle and parse learning content`.

### Task 4: Domain state, SRS, plan, stats, merge

**Files:**
- Create: `core/model/State.kt`, `core/model/Content.kt`, `core/learning/LearningEngine.kt`, `core/learning/StateValidator.kt`, `core/sync/MergeStates.kt`
- Test: `core/learning/LearningEngineTest.kt`, `StateValidatorTest.kt`, `core/sync/MergeStatesTest.kt`
- Create: `app/src/test/resources/state-v3.json`

**Interfaces:**
- Produces:
  - `@Serializable data class AppState(version=3, settings=Settings(), records=emptyMap(), sentenceRecords=emptyMap(), papers=emptyMap(), drafts=emptyMap(), savedWords=emptyList(), savedSentences=emptyList(), events=emptyList())`
  - `fun rateWord(state, id, quality, at=now): AppState`
  - `fun recordSentence(state, id, answer, rating, at=now): AppState`
  - `fun buildQueue(words, state, mode, at): List<WordDto>`
  - `fun statistics(state, words, at): Statistics`
  - `fun dailyPlan(state, words, sentences, at): DailyPlan`
  - `fun mergeStates(local, remote): AppState`
  - `fun validate(state): ValidationResult`

- [ ] Port `rateWord`: again interval 0/due +10min/streak 0/lapses+1/difficult; hard interval 1 day/streak 0/difficult; good max(3, old*2.2); easy max(7, old*2.8); cap 365; append word event with `isNew`.
- [ ] Port `recordSentence` (answer ≤10000, rating review/mastered), `gradePaper`, `checkSpelling`, `buildQueue` ordering (review due asc; new by paper hits; saved/mistakes by lastReview asc; all by lastReview asc).
- [ ] Port `statistics` (today words/sentences/papers, focus minutes, learned/mastered/difficult, due, streak, week, activeDays) and `dailyPlan` (review/new/sentences steps with done/total/pending).
- [ ] Port `mergeStates` exactly: settings local; records by lastReview; sentenceRecords/papers by at; drafts preference; bookmark union; event dedupe key `type|id|quality/rating/seconds|at`, sort, keep last 100000.
- [ ] Port validation rules: ID regexes, goals 1–100/1–20/1–120, event limits, text limits, paper ranges, v1–v3 acceptance; write v3 only; legacy `w001…w090` mapping.
- [ ] Create a golden v3 fixture from the web model (records for two words, one sentence, one paper, one draft, saved items, focus/word/sentence/paper events).
- [ ] Write failing tests first for each formula/ordering/merge/validator, run to confirm red, implement, then run green.
- [ ] Commit: `feat: port learning state machine and sync merge`.

### Task 5: Room, state mapper, progress repository

**Files:**
- Create: `data/local/Entities.kt`, `Daos.kt`, `DailyEnglishDatabase.kt`, `ProgressRepository.kt`, `core/sync/StateMapper.kt`
- Test: `app/src/androidTest/java/com/dailyenglish/native/data/local/ProgressRepositoryTest.kt` (in-memory Room on device), `app/src/test/java/com/dailyenglish/native/core/sync/StateMapperTest.kt`

**Interfaces:**
- Produces:
  - `@Database(entities=[WordRecordEntity, SentenceRecordEntity, PaperRecordEntity, DraftEntity, SavedWordEntity, SavedSentenceEntity, StudyEventEntity, SettingsEntity], version=1, exportSchema=true)`
  - `class StateMapper @Inject constructor(db, daos, json)` with `suspend fun snapshot(): AppState`, `suspend fun replaceAll(state)`, `suspend fun applyRemote(state)`.
  - `class ProgressRepository @Inject constructor(mapper, daos, syncManager, ...)` with `val state: StateFlow<AppState>`, `suspend fun load()`, `fun rateWord`, `fun recordSentence`, `fun setDraft`, `fun toggleSavedWord/Sentence`, `fun updateSettings`, `fun addFocus`.

- [ ] Define entities exactly as spec §6.1; indices on `study_events(at)`, `study_events(type, exam)`.
- [ ] Implement DAOs: snapshot queries, upsert word/sentence/paper/draft, insert/delete saved, insert event, observe counts, clearAll, settings upsert.
- [ ] Implement `StateMapper.snapshot()` from tables; `replaceAll()` in `db.withTransaction` clearing and inserting; map JSON strings for paper answers/wrong/words.
- [ ] Implement `ProgressRepository`: load snapshot into `MutableStateFlow`; mutations use `LearningEngine`, update in-memory state, persist only affected rows + event, then `syncManager.markDirty()`.
- [ ] Persist drafts with 400ms debounce in repository; ensure final draft survives process death.
- [ ] Tests: mapper round-trip golden fixture; rateWord writes one record + one event; saved toggle idempotent; remote replace clears stale rows.
- [ ] Run tests; commit: `feat: add Room progress store and state mapper`.

### Task 6: Network, token store, auth

**Files:**
- Create: `data/remote/ApiDtos.kt`, `ApiService.kt`, `RemoteDataSource.kt`, `NetworkModule.kt`
- Create: `data/local/TokenStore.kt`, `SessionPreferences.kt`
- Create: `data/repository/AuthRepository.kt`
- Test: `data/remote/RemoteDataSourceTest.kt`, `data/repository/AuthRepositoryTest.kt`

**Interfaces:**
- Produces:
  - `interface ApiService` with suspend `register`, `login`, `logout`, `me`, `getState`, `putState`, `changePassword`.
  - `sealed class ApiResult<T> { data class Ok, data class Error(status, message, payload?) }`
  - `class TokenStore @Inject constructor(dataStore, cipher)` with `suspend fun token(): String?`, `save`, `clear`.
  - `class AuthRepository` with `login/register/logout/me/changePassword`, `val session: StateFlow<SessionState>`.
- `buildConfigField("String", "API_BASE_URL", "\"https://101.37.23.76:10444/\"")`.

- [ ] Implement serializable DTOs matching spec §7.1: `AuthResponse(user, token)`, `StateResponse(revision, payload: AppState?)`, `PutStateRequest(revision, payload)`, `RevisionResponse(revision)`, `ErrorResponse(error)`.
- [ ] OkHttp interceptor adds `x-client` always and `Authorization` when token exists; JSON content type; no logging of bodies/tokens.
- [ ] Retrofit with kotlinx.serialization converter; timeouts 15s; base URL from BuildConfig.
- [ ] Implement Keystore AES-GCM cipher and `TokenStore`; DataStore key `token_ciphertext`; clear on decrypt failure.
- [ ] Implement `SessionPreferences` with `guestMode`.
- [ ] `AuthRepository`: login/register save token; logout clears; 401 clears token; map server error strings; `me()` returns email.
- [ ] Tests with MockWebServer: login sends `x-client`, returns token; authenticated call sends Bearer; 401 maps and clears token; 409 state payload parses.
- [ ] Run tests; commit: `feat: add encrypted token auth and API client`.

### Task 7: Sync manager and WorkManager

**Files:**
- Create: `data/repository/SyncManager.kt`, `SyncWorker.kt`
- Modify: `DailyEnglishApplication.kt`, `AndroidManifest.xml`
- Test: `data/repository/SyncManagerTest.kt`

**Interfaces:**
- Produces:
  - `enum class SyncMode { ANONYMOUS, MEMBER }`, `enum class SyncStatus { IDLE, SAVING, DIRTY, OFFLINE, SIGNED_OUT }`
  - `data class SyncUiState(mode, status, email, problem, lastSyncedAt)`
  - `class SyncManager` with `val uiState: StateFlow<SyncUiState>`, `suspend fun start()`, `fun markDirty()`, `suspend fun syncNow(): Boolean`, `suspend fun onLogin()`, `fun onLogout()`.

- [ ] Implement `start()`: load token; if none set anonymous; else `me()`; on 200 set member and `reconcile()`; on 401 clear token/anonymous; on network error set offline/local.
- [ ] `reconcile()`: GET state; revision 0 → upload local snapshot; revision >0 → `StateMapper.applyRemote(payload)`, update revision; 401 clears token.
- [ ] `markDirty()`: debounce 1500ms; only member; set DIRTY; on flush PUT snapshot with revision; 200 update revision; 409 merge + replaceAll + retry once; 401 signed-out; network error OFFLINE and enqueue worker.
- [ ] WorkManager: `@HiltWorker SyncWorker` calls `syncNow`; unique periodic work with network constraint when offline/dirty; cancel on success. Configure `HiltWorkerFactory` via `Configuration.Provider`.
- [ ] State flow drives top-bar sync chip and settings account section.
- [ ] Tests with fake RemoteDataSource/StateMapper: 200 success, 401 clears, 409 merges and retries, offline keeps local and marks offline.
- [ ] Build; commit: `feat: add local-first sync manager and worker`.

### Task 8: Auth and app bootstrap UI

**Files:**
- Create: `feature/auth/AuthViewModel.kt`, `AuthScreen.kt`
- Create: `ui/AppViewModel.kt`, `ui/AppRoot.kt`
- Modify: `MainActivity.kt`, `MainScaffold.kt`

**Interfaces:**
- Produces: `AppUiState(booting, showAuth, guestMode, sync, email)`; `AuthUiState(mode, email, password, busy, error)`.

- [ ] Implement `AppViewModel` bootstrap: load guestMode/token; `sync.start()`; expose whether to show auth or main; continue-guest sets guestMode and shows main.
- [ ] Auth screen: login/register segmented control, email/password fields, validation (email shape, password min 8 with letters/digits), submit, error text, loading, guest button, brand header. Use M3 components.
- [ ] Settings account section: email + sync status + sync now + logout; guest shows login entry.
- [ ] Wire AppRoot: booting splash/progress → auth screen or MainScaffold; status bar/insets.
- [ ] Manual verification on device: fresh install shows auth; guest enters Today; logout returns guest.
- [ ] Commit: `feat: add auth and guest bootstrap UI`.

### Task 9: Study UI, TTS, focus

**Files:**
- Create: `feature/today/TodayScreen.kt`, `TodayViewModel.kt`
- Create: `feature/words/WordsScreen.kt`, `WordsViewModel.kt`, `LibraryScreen.kt`
- Create: `feature/review/ReviewScreen.kt`
- Create: `feature/sentences/SentencesScreen.kt`, `SentencesViewModel.kt`
- Create: `feature/settings/SettingsScreen.kt`, `SettingsViewModel.kt`
- Create: `feature/placeholder/ComingSoonScreen.kt`
- Create: `data/tts/TtsManager.kt`, `ui/components/FocusTimerSheet.kt`, common components.
- Modify: `ui/navigation/AppNavHost.kt`, `MainScaffold.kt`.

**Interfaces:**
- Produces: full M1 user flow with `StudyViewModel` or feature VMs; `TtsManager.speak(text): Result<Unit>`; focus timer events.

- [ ] Today: date/greeting, exam SegmentedButton, three plan steps with counts, active card (word/sentence/done), continue action, streak/minutes footer; actions route to Practice/Words/Review/Sentences.
- [ ] Words: flashcard front/back (serif headword, phonetic, meaning, example, forms), reveal, four rating buttons with labels 忘记/模糊/记住/熟悉, TTS, save, progress `n/total`, next; spelling mode with text field + check + reveal; library with search, filter all/new/learned, 60/page, speak/save/open.
- [ ] Review: due list sorted by due, start review, upcoming summary, empty state; review session uses Words screen mode=review.
- [ ] Sentences: catalog, prompt (en/zh), multiline answer with debounced draft, reveal reference + analysis/keywords, self-assess, save, next; TTS for en prompts.
- [ ] Settings: dailyGoal 1–100, sentenceGoal 1–20, focusMinutes 1–120, exam, save; account/sync; data note; placeholders for backup/password (M3).
- [ ] Focus timer: top-bar action opens sheet; start/pause/reset; per-minute checkpoint calls `addFocus`; close app ends timer; show remaining in top bar.
- [ ] TTS: init TextToSpeech, Locale.US, rate 0.86, QUEUE_FLUSH; check language availability; expose errors to snackbar; shutdown in Application? Keep singleton.
- [ ] Notebook/Stats: ComingSoon screens with clear “第二阶段/第三阶段” copy; no fake stats.
- [ ] Edge-to-edge/insets, dark theme, content descriptions, 48dp targets; snackbar host for errors/success.
- [ ] Build and install; verify guest flow, word rating persists after restart, spelling, sentence draft/reveal/self-assess, TTS audio, focus minutes.
- [ ] Commit: `feat: implement M1 study flows with native TTS`.

### Task 10: Verification, APK, documentation

**Files:**
- Modify: `README.md`
- Create: `app/src/test/java/...` remaining tests
- Create: `dist/daily-english-native-0.1.0-debug.apk` via build script or copy.

**Interfaces:**
- Produces: verified debug APK and handoff.

- [ ] Run `gradlew.bat testDebugUnitTest`; all tests pass.
- [ ] Run `gradlew.bat assembleDebug`; verify package `com.dailyenglish.native`, versionName `0.1.0`, versionCode 1, min/target 24/36, content assets present.
- [ ] Run `gradlew.bat connectedDebugAndroidTest` for the in-memory Room test on PMW110.
- [ ] Install on PMW110 with `adb install -r`; run manual checklist:
  - fresh auth screen → guest → today;
  - API `/api/me` 401 with no banner breaking guest;
  - word reveal/rate → restart → progress kept;
  - spelling correct/incorrect;
  - sentence draft/reveal/self-assess;
  - TTS `can` produces AudioTrack output;
  - focus timer writes minutes;
  - settings persist; sync claims guest/local;
  - dark mode and font scale 1.3;
  - no crash in logcat.
- [ ] Copy APK to `dist/daily-english-native-0.1.0-debug.apk`; compute SHA-256.
- [ ] Update README with build, architecture, API, content copy, M1 scope, test commands, device notes.
- [ ] Commit: `docs: add native build and verification notes`; tag `v0.1.0-m1`.
- [ ] Report actual verification evidence and any limitation (successful account sync requires a real account; papers/notes/stats deferred).

## Self-Review

- Spec coverage: M1 screens, auth/sync, Room/mapper, content, TTS, focus, M3, tests, APK are all mapped to Tasks 1–10; M2/M3 are explicitly out of this plan.
- No placeholders: each task names files, interfaces, behaviors, tests and verification commands; dependency versions are resolved from the generated catalog during Task 1 and recorded in the version catalog.
- Type consistency: `AppState`, `WordDto`, `SentenceDto`, `SyncUiState`, `StateMapper` names are used consistently across tasks; JSON fields match spec §6.2.
