# Daily English Native

原生 Android 版 Daily English，使用 Kotlin、Jetpack Compose 和 Material 3 重写。
当前处于 **M1（核心学习）** 开发阶段，与现有 Capacitor 版并行安装。

## 工程标识

- Application ID：`com.dailyenglish.native`
- Kotlin namespace / package：`com.dailyenglish.nativeapp`
  （`native` 是 Java 关键字，不能作为包名；applicationId 不受此限制）
- 桌面名：`Daily English Native`
- 版本：`0.1.0`（versionCode 1）

## 技术栈

- Kotlin 2.3.x、Compose BOM、Material 3、Navigation 3
- Hilt、Room、DataStore、Retrofit/OkHttp、kotlinx.serialization、WorkManager
- 原生 TextToSpeech

## 构建

前置：Android Studio JBR 21、Android SDK 36、设备/模拟器。

```powershell
$env:JAVA_HOME = "D:\Software\Tool\AndroidStudio\jbr"
cd native-android
.\gradlew.bat assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`。

## 后端

- API：`https://101.37.23.76:10444/`
- 移动请求头：`x-client: daily-english-mobile`
- 登录后：`Authorization: Bearer <token>`
- 状态协议：与网页版一致的 version 3 JSON；服务端 `/api/state` 乐观锁。
- token 使用 Android Keystore 加密后存入 DataStore。

## M1 范围

认证/游客、Today、词卡（闪卡/拼写/词库）、复习、句子、TTS、专注计时、
学习设置、Room 本地进度、账号同步。真题、笔记、统计详情在后续阶段。

## 内容

内容来自上级目录 `web/data`（7,291 词、253 句、44 套真题），构建时复制到
`app/src/main/assets/content/`；更新内容需要重建 APK。

## 文档

- 设计规格：`docs/superpowers/specs/2026-10-18-native-android-design.md`
- M1 实施计划：`docs/superpowers/plans/2026-10-18-native-android-m1.md`
