# Native Android 进度存档

更新时间：2026-10-18 18:30（CST）  
状态：**M1 完成并已真机验证；任务暂停，代码已提交。**

## 仓库与交付

- 工程：`native-android/`（独立 git 仓库，当前分支 `main`）
- 最近提交：`7cc3d9a feat: finalize M1 with device-verified fixes`
- 标签：`v0.1.0-m1`
- APK：`app/build/outputs/apk/debug/app-debug.apk`
- 桌面副本：`C:\Users\fjh28\Desktop\Daily-English-Native-0.1.0-debug.apk`
- SHA-256：`bf6afd65bd30fd96f218e2c0900185ce7b462b701bc854fb5d46604dd77fa32d`
- 设备：PMW110 / Android 17（API 37）；APK 已安装，停留在认证首屏。

## 已完成（M1）

- 认证/注册/游客；真实后端 `https://101.37.23.76:10444`，Bearer + `x-client`。
- 今日计划、考试切换、词卡、拼写、词库搜索/筛选、复习、句子翻译与自评。
- 原生 TTS、专注计时、学习设置。
- Room 本地进度；Keystore 加密会话；version 3 状态 JSON；409 合并；WorkManager 重试。
- Material 3 品牌主题、品牌启动图标、edge-to-edge、底部导航与 Navigation 3。

## 验证证据

- JVM 单测：33/33 通过。
- 设备测试：5/5 通过（Room、Keystore、WorkManager、同步/认证相关）。
- 真机：游客/词卡评分/冷启动保留/拼写隐藏单词/句子草稿与自评/TTS 实际出声/专注倒计时。
- 真实网络：`POST /api/login` 抵达服务器并返回 401；服务端日志 UA `okhttp/4.12.0`。
- 修复：Manifest 缺少 `TTS_SERVICE` 查询导致系统 TTS 引擎不可见，已加入 `<queries>`。
- 无崩溃日志；TTS 修复后 AudioTrack 实际播放 15360 帧。

## 未完成 / 下次继续

1. **真实账号成功登录与跨端同步**：需要用户在 App 内登录一次；验证 token 写入、
   `/api/state` 拉取/上传、409 合并、离线重试。未创建测试账号。
2. **M2 真题**：解析 44 套真题 JSON，完成套题/分组、完形、阅读、翻译、判分、自评。
3. **M3 收尾**：笔记、完整统计/热力图、备份导入导出、修改密码、平板双栏。
4. **发布**：生成 release keystore，替换 debug 签名；评估是否替换 `com.dailyenglish.app`。

## 恢复命令

```powershell
$env:JAVA_HOME = "D:\Software\Tool\AndroidStudio\jbr"
cd native-android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

设备测试：

```powershell
.\gradlew.bat connectedDebugAndroidTest   # 测试结束后会卸载 App，需重新安装 APK
```

## 注意

- 不要在 `android-app/`（Capacitor 版）与 `native-android/` 之间复制 token 或数据库；
  两端只通过账号同步。
- 内容更新：修改 `../web/data` 后重新构建，`syncContent` 任务会复制到 assets。
- IP 证书与后端入口由服务器维护；App 端只依赖 `API_BASE_URL`。
