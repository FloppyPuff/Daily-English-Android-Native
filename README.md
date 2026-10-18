# Daily English Native

Daily English 的原生 Android 版：Kotlin + Jetpack Compose + Material 3。
与现有 Capacitor 版并行安装，已实现 **M1（核心学习 + 账号同步）、M2（真题）与 M3（1.0.0 完整对等）**。

## 项目标识

- Application ID：`com.dailyenglish.native`
- Kotlin namespace / package：`com.dailyenglish.nativeapp`
  （`native` 是 Java 关键字，不能作为包名；applicationId 不受此限制）
- 桌面名：`Daily English Native`
- 版本：`1.0.1`（versionCode 4）

## M1 已实现

- 登录/注册/游客模式；真实后端 `https://101.37.23.76:10444`。
- 今日计划、考试方向切换、连续学习与今日专注统计。
- 词卡：正面回忆、释义/例句/词形、四档自评、跳过、换一组。
- 拼写自测：正面隐藏单词，只给释义；检查拼写后自评。
- 词库：7,291 词、搜索、全部/未学习/已学习筛选、60 词/页。
- 复习：到期列表与开始复习。
- 句子：85 条 CET-6 句子（全部方向共 253 条）、译文自动保存、参考译文、句法拆解、重点词、自评。
- 原生 TTS（系统英语语音包）；专注计时器；学习设置。
- Room 本地进度、Keystore 加密会话、version 3 状态 JSON、409 合并、WorkManager 重试。
- 笔记与记录在 M3 启用。

## M2 已实现

- 44 套考研真题（1998–2025，英语/英语一/英语二），按年份排序与方向筛选。
- 套题页按 section 分组，显示题型、题数、分值、完成状态与来源说明。
- 完形/阅读逐题选项作答、提交批改、正确/错误高亮与得分。
- 完形原文空格随作答实时回填；阅读 Part B 仅原文时明确标注、不提供作答。
- 段落翻译/划线句子翻译：输入自动保存、对照参考译文、自我评估。
- 真题记录写入 `papers` 并随 version 3 状态同步；冷启动保留。

## M3 已实现

- 笔记：收藏词/句、薄弱 & 错题（含真题错词）、搜索、进入单条练习、宽屏双栏。
- 记录：累计已学/较熟悉/学习天数/专注分钟/真题、7 日词汇节奏、掌握比例、12 周横向热力图（周一至周日，按周排列）。
- 备份导出/恢复：完整 version 3 JSON、5 MB 上限、导入前二次确认、导入失败不改动现有数据。
- 修改密码：当前/新密码校验、服务器接口、其他设备会话失效提示。
- 自适应：`>=600dp` 使用 NavigationRail，记录/笔记在宽屏使用双栏。
- 外观：可跟随系统壁纸取色（Android 12+），默认保留品牌绿色；偏好仅存本机。

## 技术栈

Kotlin 2.3、Compose BOM 2026.03、Material 3、Navigation 3、Hilt、Room、DataStore、
Retrofit/OkHttp、kotlinx.serialization、WorkManager、Android TextToSpeech。
单 `app` 模块，按 `core/data/ui/feature` 分包。

```
native-android/
  app/src/main/assets/content/     # 构建时从 ../web/data 同步，不提交
  app/src/main/java/com/dailyenglish/nativeapp/
    core/model|learning|sync       # 状态、SRS、统计、校验、合并
    data/content|local|remote      # 内容、Room、会话、Retrofit
    data/repository                # 进度、认证、同步
    feature/auth|study|settings    # Compose 页面与 ViewModel
    feature/papers                 # 真题列表、套题、分组练习
    feature/notebook|stats         # 笔记、学习记录
    ui/theme|navigation            # 品牌主题、Navigation 3、NavigationRail
  app/src/test/                    # 49 个 JVM 单测
  app/src/androidTest/             # 7 个设备测试
```

## 构建

前置：JDK 21（Android Studio JBR）、Android SDK 36、已连接设备。

```powershell
$env:JAVA_HOME = "D:\Software\Tool\AndroidStudio\jbr"
cd native-android
.\gradlew.bat assembleDebug
```

内容同步任务会自动把 `../web/data` 复制到 `app/src/main/assets/content/`。
APK：`app/build/outputs/apk/debug/app-debug.apk`。

当前交付（2026-10-18）：桌面副本
`C:\Users\fjh28\Desktop\Daily-English-Native-1.0.1-debug.apk`，
SHA-256 `7045b9b4ce56f3fa7f9cc8b5552d1dfd3e2902900ab113d1e26e1ee2be2eff5c`。
重新构建后校验和会变化。

## 测试

```powershell
.\gradlew.bat testDebugUnitTest          # 49 tests
.\gradlew.bat connectedDebugAndroidTest  # 7 tests on device
```

已覆盖：SRS 四档间隔与上限、队列排序、每日计划、统计/热力图、状态校验、v1 词头迁移、
merge 冲突、状态 JSON 往返、真题 DTO 解析/分组 key/判分/记录、笔记筛选、
备份编解码/非法版本/未知词句、密码规则、Room 持久化/导入替换、
MockWebServer 登录/401/409、Keystore 会话加密、WorkManager 初始化。

## 账号与同步

- 所有请求带 `x-client: daily-english-mobile`；登录后 Bearer Token。
- 登录/注册成功时服务端返回 token；token 用 Android Keystore AES-GCM 加密后存入 DataStore。
- 启动时 `/api/me` 判断会话；`/api/state` 读取 revision 与 payload；PUT 使用乐观锁。
- 409 时按网页 `mergeStates` 语义合并并重试；离线进入 OFFLINE 并安排 15 分钟网络约束重试。
- 状态 JSON 与网页版一致（version 3），支持 v1 `w001…w090` 迁移。

## 真机验证（2026-10-18）

设备：PMW110 / Android 17（API 37）。

- 安装包名独立，未覆盖现有 `com.dailyenglish.app`。
- 认证首屏、游客模式、今日计划正常。
- 词卡显示/释义/“记住”自评；冷启动后进度保留（已学 1 词、连续 1 天）。
- 拼写模式正面只显示释义，不泄露单词。
- 句子输入自动保存、参考译文展开、自评后今日句子从 0/3 → 1/3。
- 原生 TTS 实际出声：AudioTrack 15360 帧，系统英语语音包可用。
- 专注计时从 25:00 正常倒计时并可暂停。
- 真实登录请求抵达后端：`POST /api/login` 401（错误密码），客户端显示
  “邮箱或密码不正确”；服务端日志 UA 为 `okhttp/4.12.0`。
- `connectedDebugAndroidTest`：5/5 通过；JVM 单测 33/33 通过。

### M2（2026-10-18）

- 真题目录 44 套；全部/英语二/英语一/英语筛选正常；年份徽标居中。
- 2025 英语二：6 组；2025 英语一：6 组；2005 英语：7 组（含 Part B 仅原文）。
- 完形 20 题全答后提交，显示 `已批改 5 / 20 题`；阅读 5 题提交显示 `3 / 5`
  并高亮正确答案、标出错误项；选中项边框清晰可辨。
- 翻译输入、参考译文展开、`已经理解` 自评与提示均正常。
- Part B 显示“仅公开答案字母…只留原文精读”，无作答控件。
- 记录在返回套题页、冷启动/覆盖安装后保留；JVM 37/37、设备 6/6 通过。

### M3（2026-10-18）

- 笔记：收藏 1 个单词后出现在“我的收藏”；薄弱/错题空态与规则正常；点击单词进入单词练习。
- 记录：累计已学 11 词、7 日柱状图、掌握比例、横向 12 周热力图和专注/真题卡片均渲染真实数据。
- 备份：导出 `daily-english-2026-10-18.json`（5,100 B）；该文件通过网页版 `parseBackup` 校验，
  记录数/事件数与原生一致。恢复逻辑有编解码单测与 Room 导入设备测试。
- 修改密码：表单校验（空当前密码、弱新密码）与错误提示正常；未实际改动真实账号密码。
- 横屏：`>=600dp` 显示 NavigationRail（图标在短屏不裁切）；恢复竖屏后自动旋转设置已还原。
- 深色模式：认证页与统计页使用深色品牌色板，切回浅色正常。
- 动态取色：Android 12+ 开关即时切换 Material You 配色，关闭恢复品牌绿；偏好存本机。
- 用户已用真实账号完成登录与跨端同步验证。
- JVM 49/49、设备 7/7 通过。

## 已知边界

- 当前为 **debug 签名**；长期分发前需 release keystore。
- 备份恢复的系统文件选择器依赖设备文件管理 UI，未做自动化点击验证；
  文件格式、校验、导入替换和网页兼容性已由测试覆盖。
- 平板/折叠屏双栏在代码中按 `>=720dp` 启用，本次仅在手机横屏验证 NavigationRail；
  未在物理平板或折叠屏上实测。
- 大字体的系统级字体缩放未做实机截图验证。
- 朗读依赖系统英语语音包；缺失时提示。Manifest 已声明 `TTS_SERVICE` 查询。
- 内容随 APK 打包；题库更新需重建。
- 早期真题部分 section 缺少分值，界面只显示题数。
- 旧版设备测试会清除本机登录会话；`SessionStoreTest` 已改为测试后恢复原会话。
  如果运行过旧测试，请在 App 内重新登录，账号数据不受影响。

## 文档

- 设计规格：`docs/superpowers/specs/2026-10-18-native-android-design.md`
- M1 实施计划：`docs/superpowers/plans/2026-10-18-native-android-m1.md`
