# Daily English Native

Daily English 的原生 Android 版：Kotlin + Jetpack Compose + Material 3。
与现有 Capacitor 版并行安装，当前完成 **M1（核心学习 + 账号同步）**。

## 项目标识

- Application ID：`com.dailyenglish.native`
- Kotlin namespace / package：`com.dailyenglish.nativeapp`
  （`native` 是 Java 关键字，不能作为包名；applicationId 不受此限制）
- 桌面名：`Daily English Native`
- 版本：`0.1.0`（versionCode 1）

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
- 笔记/记录入口显示下一阶段空态（M3）。

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
    ui/theme|navigation            # 品牌主题、Navigation 3
  app/src/test/                    # 33 个 JVM 单测
  app/src/androidTest/             # 5 个设备测试
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

## 测试

```powershell
.\gradlew.bat testDebugUnitTest          # 33 tests
.\gradlew.bat connectedDebugAndroidTest  # 5 tests on device
```

已覆盖：SRS 四档间隔与上限、队列排序、每日计划、统计、状态校验、v1 词头迁移、
merge 冲突、状态 JSON 往返、Room 持久化、MockWebServer 登录/401/409、
Keystore 会话加密、WorkManager 初始化。

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

## 已知边界

- 成功登录、token 写入与跨设备同步尚未用真实账号实测；错误密码路径已验证。
- WorkManager 初始化与调度器注入已验证；离线重试的完整链路需要登录账号后观察。
- 真题（M2）、完整笔记/统计/备份/修改密码/平板双栏（M3）尚未实现。
- 当前为 **debug 签名**；长期分发前需 release keystore。
- 朗读依赖系统英语语音包；缺失时提示。Manifest 已声明 `TTS_SERVICE` 查询。
- 内容随 APK 打包；题库更新需重建。

## 文档

- 设计规格：`docs/superpowers/specs/2026-10-18-native-android-design.md`
- M1 实施计划：`docs/superpowers/plans/2026-10-18-native-android-m1.md`
