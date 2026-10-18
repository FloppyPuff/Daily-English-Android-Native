# Daily English 原生 Android 设计规格

日期：2026-10-18  
状态：设计已确认，待规格复核  
目标工程：`native-android/`  
Application ID：`com.dailyenglish.native`（与现有 Capacitor 版并行）  
Kotlin namespace / package：`com.dailyenglish.nativeapp`（`native` 是 Java 关键字，不能作包名）  
开发期桌面名：`Daily English Native`（正式名称与最终包名后续再定）

## 1. 背景与目标

现有 Daily English 由 Web/PWA、Node/SQLite 后端和 Capacitor 外壳组成。本次新建一个完全原生的 Android 工程，使用 Kotlin、Jetpack Compose 和 Material 3 重写客户端；后端、账号、同步协议和内容语料保持不变。

首版必须做到：

- 原生 UI，不使用 WebView。
- 复用现有账号体系：`https://101.37.23.76:10444`，Bearer Token，`x-client: daily-english-mobile`。
- 学习进度与网页/Capacitor 版使用同一份 version 3 状态 JSON，可跨端同步和冲突合并。
- 词库、句子、真题内置，断网也能完成词卡、复习和句子练习。
- Material 3 的配色、排版、组件、导航、无障碍和深浅色适配；保留鼠尾草绿/纸白的品牌气质。
- 分阶段交付可安装 APK：M1 核心学习与账号同步，M2 真题，M3 笔记/统计/收尾。

## 2. 非目标

- 不修改后端 API、数据库结构或同步协议。
- 不做内容热更新；内容更新随 APK 重建。
- 不做 iOS、上架签名、应用商店发布。
- 不替换现有 `com.dailyenglish.app`；两个应用并行安装和对比。
- 不迁移 Capacitor WebView 的 localStorage 游客数据；跨端迁移走账号同步或后续备份导入。

## 3. 技术栈与工程结构

- Kotlin 2.x，Jetpack Compose，Material 3。
- Navigation Compose（类型安全路由），ViewModel + StateFlow，Coroutines/Flow。
- Hilt + KSP 依赖注入。
- Room 持久化学习进度；DataStore 保存 token、同步元数据和游客模式标记。
- Retrofit + OkHttp + kotlinx.serialization 访问后端；WorkManager 负责离线重试。
- AndroidX SplashScreen、Activity Result API（备份导入导出，M3）。
- AGP 9 官方 `empty-activity` 模板；minSdk 24，compileSdk/targetSdk 36；JDK 21（Android Studio JBR）。
- Gradle Kotlin DSL + version catalog；阿里云 Google/Public Maven 镜像优先。
- 单 `app` 模块，按 feature 分包；不先做多模块。数据层和领域层保持清晰边界，后续可拆模块。

```
native-android/
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      assets/content/                 # 构建时从 ../web/data 同步
      java/com/dailyenglish/native/
        DailyEnglishApplication.kt
        MainActivity.kt
        core/model/                   # 状态、内容、事件模型
        core/learning/                # SRS、每日计划、统计、判分
        core/sync/                    # 状态校验、mergeStates、映射
        data/content/                 # assets 解析与真题懒加载
        data/local/                   # Room、DAO、DataStore、TokenStore
        data/remote/                  # Retrofit API、DTO、拦截器
        data/repository/              # Progress/Auth/Sync 仓库
        ui/theme/                     # 品牌色、排版、形状
        ui/navigation/                # 路由、底部栏/导航栏
        ui/components/                # 共用卡片、空态、错误态、计时器
        feature/auth/
        feature/today/
        feature/words/
        feature/sentences/
        feature/review/
        feature/papers/               # M2
        feature/notebook/             # M3
        feature/stats/                # M3
        feature/settings/
    src/test/                         # JVM 单测与 golden fixtures
    src/androidTest/                  # Compose/真机测试（按需）
```

依赖版本在实施计划阶段锁定为 Maven 实际可用的稳定版，并优先验证 Hilt/KSP 与 AGP 9 的兼容性。

## 4. 架构与数据流

UI → ViewModel → Repository → Room/DataStore/Assets/API，单向数据流。

- ViewModel 暴露不可变 `UiState`；事件通过方法进入，不直接改 Room。
- Repository 是唯一写入入口；每次学习操作在同一事务中更新 Room，并标记 `dirty`，随后触发防抖同步。
- Domain 层为纯 Kotlin，不依赖 Android；SRS、每日计划、统计、判分、状态校验和 merge 全部可单测。
- Hilt 绑定：`ContentRepository`、`ProgressRepository`、`AuthRepository`、`SyncManager`、`ApiClient`、`TokenStore`。
- `SyncManager` 单例持有同步状态机：`anonymous`、`idle`、`saving`、`dirty`、`offline`、`signed-out`；UI 只观察状态。

## 5. 内容数据

内容来自 `web/data`，构建时复制到 `app/src/main/assets/content/`：

- `words.json`：7,291 词，字段包括 id、phonetic、pos、meaning、senses、exams、base、extra、collins、oxford、freq、forms、hits、postgradYears、cet6Years、example、tiers。
- `sentences.json`：253 句，字段包括 id、exams、kind、direction、level、topic、en、zh、analysis、keywords、from、number、source。
- `papers/index.json`：44 套真题索引；`papers/<id>.json`：sections → groups → questions/passage，单套约 25KB。

实现要求：

- Gradle `SyncContent` 任务从 `rootProject.projectDir.parentFile/web/data` 复制到 assets，作为 `preBuild` 依赖；目录缺失时构建失败并给出明确错误。
- 使用 kotlinx.serialization，`ignoreUnknownKeys = true`；所有可选字段有显式默认值。
- 启动时在 IO 线程解析词库和句子；真题按 set id 懒加载并缓存。
- 内容加载状态为 `Loading/Ready/Error`；Error 显示可重试页面，不阻塞本地进度读取。
- 不把内容写入 Room；内容只读，避免无意义的迁移。

## 6. 本地数据与同步契约

### 6.1 Room 表

- `word_records(id PK, attempts, intervalDays, due, streak, lapses, difficult, lastReview)`
- `sentence_records(id PK, answer, rating, at)`
- `paper_records(key PK, answersJson, correct, total, wrongJson, wordsJson, rating, at)`
- `drafts(id PK, text)`
- `saved_words(wordId PK)`
- `saved_sentences(sentenceId PK)`
- `study_events(id AUTO PK, type, itemId, quality, rating, seconds, correct, total, exam, at, isNew)`
- `settings(id=1 PK, exam, dailyGoal, sentenceGoal, focusMinutes)`

Room version 1；导出 schema；不使用破坏性迁移作为默认策略。P1 只有 v1，后续迁移必须显式编写。

DataStore 保存：加密 token、`revision`、`lastSyncedAt`、`guestMode`。token 使用 Android Keystore AES-GCM 加密后存入 DataStore，不使用明文。

### 6.2 同步状态 JSON（version 3）

原生写出和读取的结构必须与网页一致：

```json
{
  "version": 3,
  "settings": { "exam": "cet6", "dailyGoal": 20, "sentenceGoal": 3, "focusMinutes": 25 },
  "records": { "<wordId>": { "attempts": 1, "interval": 3, "due": 0, "streak": 1, "lapses": 0, "difficult": false, "lastReview": 0 } },
  "sentenceRecords": { "<sentenceId>": { "answer": "", "rating": "mastered", "at": 0 } },
  "papers": { "<setId>#<section>-<group>": { "answers": {}, "correct": 0, "total": 0, "wrong": [], "words": [], "rating": "", "at": 0 } },
  "drafts": { "<sentenceId>": "" },
  "savedWords": [],
  "savedSentences": [],
  "events": [
    { "type": "word", "id": "", "quality": "good", "at": 0, "exam": "cet6", "isNew": true },
    { "type": "sentence", "id": "", "rating": "mastered", "at": 0, "exam": "cet6" },
    { "type": "paper", "id": "", "at": 0, "exam": "cet6", "correct": 0, "total": 0 },
    { "type": "focus", "seconds": 0, "at": 0, "exam": "cet6" }
  ]
}
```

约束与网页 `core.mjs` 一致：

- ID 形状：word `^(?:w\d{3}|[a-z][a-z'-]{1,31})$`；sentence `^(?:s\d{3}|\d{4}-[12]#\d{1,3}|cet6-\d{6}-\d{1,2})$`；paper `^\d{4}-[12]#\d{1,2}-\d{1,2}$`。
- dailyGoal 1–100；sentenceGoal 1–20；focusMinutes 1–120；事件上限 100,000；文本上限 10,000 字符；focus 单次 ≤ 7,200 秒。
- 读取支持 v1–v3；写出始终 v3。v1 的 `w001…w090` 通过内置 `legacy-ids` 映射为词头。
- 状态映射必须通过一个 `StateMapper`：Room 快照 → JSON；JSON → Room 事务替换。禁止 UI 直接拼 JSON。

### 6.3 冲突合并

409 时原生移植 `mergeStates` 的语义：

- 设置以本机为准。
- records 按 `lastReview` 取较新；sentenceRecords/papers 按 `at` 取较新。
- drafts 按较新的 sentenceRecord 归属；收藏取并集。
- events 按 `type|id|quality/rating/seconds|at` 去重，按时间排序，保留最后 100,000 条。
- 合并结果写回 Room，更新 revision，再重试 PUT；最多两轮，仍冲突则进入 offline/dirty，稍后重试。

## 7. 账号与同步

### 7.1 API

Base URL：`https://101.37.23.76:10444`；所有请求 HTTPS；移动请求带 `x-client: daily-english-mobile`，登录后带 `Authorization: Bearer <token>`。

| 方法 | 路径 | 请求 | 成功 | 失败 |
|---|---|---|---|---|
| POST | `/api/register` | `{email,password}` | 201 `{user:{email},token}` | 400/409/503 `{error}` |
| POST | `/api/login` | `{email,password}` | 200 `{user:{email},token}` | 401/429 `{error}` |
| POST | `/api/logout` | 空 | 204 | — |
| GET | `/api/me` | — | 200 `{user:{email}}` | 401 |
| GET | `/api/state` | — | 200 `{revision,payload}` | 401 |
| PUT | `/api/state` | `{revision,payload}` | 200 `{revision}` | 400/401/409 `{revision,payload}` |
| POST | `/api/password` | `{current,next}` | 200 `{user:{email}}` | 400/401 |

`payload` 为 6.2 的完整状态对象（服务端已校验）。`revision=0` 时 `payload` 可能为 null。

### 7.2 启动与登录流程

1. 有 token：`GET /api/me`。200 进入 member 并拉取 state；401 清除 token。
2. 无 token：首次启动显示登录/注册页，提供“暂不登录”；选择后写入 `guestMode=true`，进入 Today。
3. 登录/注册成功：保存 token，`guestMode=false`，拉取 state；服务器 revision 为 0 时上传本地 state，否则以服务器副本为准（与网页一致）。
4. 退出登录：调用 logout，清除 token，保留本地状态并回到 Today 游客模式。
5. 修改密码：成功后保留当前 token，其他设备会话失效。

### 7.3 本地优先

- 所有学习操作先写 Room，再触发同步；网络失败不阻塞学习。
- 学习操作后 1.5 秒防抖保存；保存中再有操作则标记 dirty，完成后继续。
- 离线时状态显示“离线，稍后重试”；WorkManager 在联网后重试。
- 401：清除 token，回 anonymous，保留本地进度，提示重新登录。
- 同步冲突/格式错误：保留本地数据，显示可读错误，不静默覆盖。

## 8. Material 3 界面设计

### 8.1 视觉语言

- 品牌核心：鼠尾草绿 `#758C70`、深墨绿 `#29352E`、纸白 `#F8FAF5`、浅沙 `#F4F0E4`。
- 以 `#758C70` 为种子生成 M3 light/dark 色板；显式覆盖 primary/container/surface/outline 等关键角色，避免默认紫色。
- 默认关闭动态取色，保留品牌；M3 设置在后续阶段提供“跟随壁纸”开关。
- 排版：英文词头/大标题使用 `FontFamily.Serif`；中文正文使用系统无衬线；数字使用等宽/表格数字（tnum）。字号按 M3 比例：显示 40–56sp、标题 20–28sp、正文 14–16sp、标签 11–13sp。
- 形状：卡片 16dp，输入框/按钮 12dp，底部面板 24dp 顶圆角；间距 4/8/12/16/24/32。
- 组件：Card、Filled/Outlined Button、FilterChip、SegmentedButton、TextField、NavigationBar、NavigationRail、ModalBottomSheet、AlertDialog、Snackbar、LinearProgressIndicator。
- 不使用渐变堆叠、玻璃拟态或默认模板式大数字面板；用一个清晰的“今日路径”作为签名元素。

### 8.2 导航

- 一级：今日、练习、笔记、记录（M3 才启用笔记/记录；M1 中对应入口显示明确的“下一阶段”空态）。
- 练习二级：词汇、句子、真题、复习；真题在 M2 启用。
- 顶栏：当前页面标题、考试方向切换、设置入口、专注计时入口。
- 紧凑屏用 NavigationBar；中等/扩展屏用 NavigationRail；M3 阶段为大屏提供双栏。
- 类型安全路由；Top-level 导航使用 saveState/restoreState；返回遵循 predictive back。
- 认证页为全屏，不显示底部导航。

### 8.3 页面

**认证**：品牌头、登录/注册切换、邮箱、密码、主按钮、游客入口、校验错误、加载态。
**今日**：日期、问候、考试切换、今日路径（复习/新词/句子）、当前活动卡、连续天数/今日专注、进入下一项；完成态。
**练习首页**：词汇/句子/真题/复习四张卡，显示到期数和已学数；真题 M2 可用。
**词汇**：闪卡（正面词头/音标，背面释义/例句/词形）、四档自评（忘记/模糊/记住/熟悉）、拼写模式、词库搜索/筛选/分页、朗读、收藏。
**复习**：到期列表、按到期排序、后续安排；开始复习。
**句子**：句子目录、原文/中文提示、译文输入自动保存、查看参考、句法拆解/重点词、自评（待巩固/已理解）、收藏、下一句。
**真题（M2）**：套题列表、section/group、完形/阅读/翻译、选项选择、提交批改、分数/错题、自评、真题词收藏。
**笔记（M3）**：收藏词/句、搜索、进入练习。
**记录（M3）**：累计已学/较熟悉/学习天数/专注分钟/真题、7 日节奏、掌握比例、12 周热力图。
**设置**：每日新词/句子目标、专注时长、考试方向、账号状态、立即同步、退出登录、备份导出/导入（M3）、修改密码（M3）、数据说明。
**专注计时**：M1 提供前台计时对话框/底部面板；开始/暂停/重置；每分钟写入 focus 事件；关闭应用即结束本次计时（与网页一致）。

### 8.4 状态与无障碍

- 每个页面具备 loading / empty / error / offline / success 状态。
- 可达性：48dp 触控区域、contentDescription、语义标题、动态字体、深浅色对比、减少动态效果。
- edge-to-edge + WindowInsets；键盘（IME）适配句子输入。
- 所有破坏性操作（退出、导入覆盖）二次确认；导入失败不覆盖现有数据。
- 不记录或打印 token、密码、完整备份内容。

## 9. 分阶段交付

### M1 核心学习（首个 APK，0.1.0）

认证/游客、Today、词汇（闪卡/拼写/词库）、复习、句子、TTS、基础专注计时、设置（目标/考试/账号同步）、Room + 状态映射 + 同步。笔记/记录入口显示下一阶段空态。

验收：

- 与 `com.dailyenglish.app` 并行安装，不覆盖现有应用。
- 断网可完成词卡、复习、句子；重启后进度保留。
- 登录现有账号后 `/api/state` 拉取/上传成功；409 合并逻辑有单测。
- TTS 在 PMW110 上实际出声；无英语语音包时给出提示。
- 状态 JSON 通过服务端 `parseBackup`；与网页 golden fixture 字段一致。

### M2 真题（0.2.0）

44 套索引、套题/分组、完形/阅读/翻译、批改、自评、真题词收藏、同步。

验收：所有套题可打开；选择题判分与网页一致；翻译题可自评；进度可同步；大文本不卡顿。

### M3 完整对等（1.0.0）

笔记、完整统计/热力图、专注记录完善、备份导出/导入、修改密码、平板/折叠屏双栏与 NavigationRail、动态取色开关、整体视觉打磨。

验收：功能与网页版对齐；PMW110 上深浅色、字体缩放、横竖屏、离线恢复通过；导出备份可被网页导入。

## 10. 测试策略

- 领域单测：SRS 四档间隔与上限、队列排序、DailyPlan、Statistics、Spelling、Paper 判分、StateValidator、mergeStates。
- 映射测试：web 生成的 v3 golden fixture → 原生解析 → Room → 再序列化，关键字段逐项相等；v1 legacy ID 映射。
- Room：内存数据库 DAO/事务测试；迁移测试从 v1 开始建立。
- 网络：MockWebServer 覆盖 login/me/state 200、401、409、429、超时、离线；验证 `x-client`、Bearer、revision。
- Compose：认证、词卡揭示/自评、句子自评、设置、空态/错误态；必要时截图测试。
- 真机：PMW110/Android 17 安装、游客离线、TTS、登录同步、深色、字体缩放、旋转、返回栈。
- 命令：`gradlew.bat testDebugUnitTest`、`gradlew.bat assembleDebug`；有设备时 `connectedDebugAndroidTest` 与 `adb install -r`。
- 实施计划首轮只覆盖 M1；M2/M3 在 M1 验收通过后分别生成计划，避免单个计划过大。

## 11. 风险与对策

- 原生与 JS 逻辑漂移：以 golden fixture 和服务端契约测试锁定；SRS/merge 逐条对照。
- Hilt/KSP 与 AGP 9 兼容：先建最小骨架并跑通编译，再逐层加功能；版本从 Maven 实际可用版本锁定。
- Maven 网络：保留阿里云镜像；首次构建预留下载失败排查。
- 同步冲突：严格移植 mergeStates；冲突不丢本地；最多两轮重试。
- token 安全：Keystore 加密；日志脱敏；仅 HTTPS。
- 双应用数据：并行包名隔离；游客数据不自动迁移；后续用账号或备份迁移。
- 内容解析/内存：词库只在 IO 解析一次；真题懒加载；解析失败可重试。
- 阶段范围膨胀：M1 不实现真题/笔记/统计详情；入口用明确空态，不伪造成绩。

## 12. 后续决策（不阻塞本规格）

- 是否在 1.0 后把原生版替换为 `com.dailyenglish.app`。
- release keystore、签名与分发方式。
- 内容热更新、动态取色默认值、平板双栏的进一步优化。
