# Daily English Native M2（真题）Implementation Plan

**Goal:** 让原生版完整练习 44 套考研真题：套题列表、分组、完形/阅读作答、翻译对照与自评、记录同步。

**Architecture:** 沿用 M1 的单 app 模块与 version 3 状态协议；真题内容从 assets 懒加载，答题状态进入现有 `papers` 记录；新增 `PapersViewModel` 与三条 Navigation 3 路由。

**Spec:** `docs/superpowers/specs/2026-10-18-native-android-design.md` §8.3、§9 M2  
**Tech:** Kotlin、Compose、Navigation 3、Hilt、Room、现有 `PaperRecord`/StateMapper。

## Global Constraints

- applicationId `com.dailyenglish.native`；version `0.2.0`（versionCode 2）。
- 真题 key 形状 `setId#section-group`，与网页 `paperGroups` 完全一致。
- 客观题判分规则：`options` 非空且 `answer` 长度为 1；翻译题不自动评分。
- 不可作答的 Part B（只有答案字母、无选项）只展示原文和说明。
- 继续使用 `ProgressRepository` 与 version 3 同步；不新增后端字段。
- 构建后真机验证；保持现有 M1 测试全绿。

## Tasks

### Task 1: 真题 DTO 与懒加载

**Files:** `data/content/ContentDto.kt`, `ContentRepository.kt`, tests.

- [x] 定义 `PaperDto/PaperSectionDto/PaperGroupDto/PaperQuestionDto`，字段 `type,title,passage,questions,number,options,answer,stem,score`；`options` 用 `Map<String,String>`。
- [x] `ContentRepository.paper(id)`：校验 `id` 形状，懒加载 `content/papers/<id>.json`，缓存；失败返回 null。
- [x] 测试解析 `1998-1`、`2025-2`：sections/groups/questions 非空；cloze 有 options，translation 有 answer。
- Run `testDebugUnitTest`；commit.

### Task 2: 真题领域逻辑

**Files:** `core/learning/PaperEngine.kt`, tests.

- [x] `paperGroups(paper)`：flatten sections/groups；生成 `index=si-gi`、`key=paper.id#index`、`section`、`instructions`、`sectionScore`、`answerable`、`translation`。
- [x] `answerable`：选择题（存在 options）或 written（translation_full 或存在 stem）；gap_fill 无 options/stem → 仅原文。
- [x] `gradePaper(answers, questions)`：只统计客观题，返回 correct/total 与 wrong numbers。
- [x] `recordPaper(state,key,answers,questions,words,rating,at)`：写 `PaperRecord` 和 paper event；校验 key/rating。
- [x] 测试：分组数、key 形状、三种题型答案判定、gap_fill 不可作答、rating 事件。
- Run tests；commit.

### Task 3: 进度仓库写入真题

**Files:** `core/sync/StateMapper.kt`, `data/repository/ProgressRepository.kt`, test.

- [x] `StateMapper.savePaper(key, record)` 编码 answers/wrong/words 到 PaperRecordEntity。
- [x] `ProgressRepository.recordPaper(key, answers, questions, words, rating)`：判分、写 Room、追加 event、更新 state、markDirty。
- [x] 设备测试：提交 cloze 记录，重新加载后 `papers[key]` 保留；`replaceFromRemote` 正确清除。
- Run `connectedDebugAndroidTest`；commit.

### Task 4: 真题界面与路由

**Files:** `feature/papers/PapersViewModel.kt`, `PapersScreens.kt`, `ui/navigation/AppDestination.kt`, `MainScaffold.kt`, `StudyScreens.kt`.

- [x] 路由 `Papers`、`PaperSet(id)`、`PaperGroup(setId,index)`；Practice 卡可进入；顶部返回可用。
- [x] `PapersViewModel`：load index、open paper/group、answers、pick、translation text、check、submit、rate、reset；从已有 `PaperRecord` 恢复答案与状态。
- [x] 套题列表：44 套、英语一/二筛选、每组完成状态。
- [x] 套题页：section/group 行、题型、题数/分值、状态、来源说明。
- [x] 组页：原文（blanks 显示为 `(n)`）、选择题选项、提交批改、正确/错误提示；翻译题输入、参考译文、自评；Part B 只读说明。
- [x] M3 视觉：页面标题不重复、卡片/筛选/答案状态、深色模式、滚动。
- Build；commit.

### Task 5: 真机验证与交付

- [x] 版本 0.2.0；构建；安装 PMW110。
- [x] 验证：列表 44 套；打开 2025-2；完形作答/提交/分数；阅读题；翻译参考/自评；Part B 只读；记录冷启动保留。
- [x] JVM/设备测试全绿；APK 复制桌面、SHA-256；更新 README/PROGRESS。
- Commit + tag `v0.2.0-m2`.

## Self-Review

- Spec coverage: 44 sets, groups, cloze/reading/translation, grading/self-assess, saved/missed cloze words, sync mapping. M3 notebook/heatmap remain out.
- No placeholders: all files/behaviors/tests named.
- Type consistency: `PaperGroup` key/index used by UI and `PaperRecord`; `answers` remains `Map<String,String>` in state, `Map<Int,String>` in UI.
