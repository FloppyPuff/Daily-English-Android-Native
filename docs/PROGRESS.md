# Native Android 进度存档

更新时间：2026-10-18 21:15（CST）  
状态：**M1、M2 完成并已真机验证；M3 待开始。**

## 仓库与交付

- 工程：`native-android/`（独立 git 仓库，当前分支 `main`）
- 标签：`v0.2.0-m2`
- APK：`app/build/outputs/apk/debug/app-debug.apk`
- 桌面副本：`C:\Users\fjh28\Desktop\Daily-English-Native-0.2.0-debug.apk`
- SHA-256：`4d0f9639e5c4725ee3eb79465e2e74125a106ccead6fb30e3b67d18f71c8e5f0`
- 设备：PMW110 / Android 17（API 37）；APK 0.2.0 已安装。

## 已完成（M1）

- 认证/注册/游客；真实后端 `https://101.37.23.76:10444`，Bearer + `x-client`。
- 今日计划、考试切换、词卡、拼写、词库搜索/筛选、复习、句子翻译与自评。
- 原生 TTS、专注计时、学习设置。
- Room 本地进度；Keystore 加密会话；version 3 状态 JSON；409 合并；WorkManager 重试。
- Material 3 品牌主题、品牌启动图标、edge-to-edge、底部导航与 Navigation 3。

## 已完成（M2 真题）

- 44 套真题解析与懒加载；真题 DTO、`PaperEngine`、`PapersViewModel`、三条路由。
- 目录筛选（全部/英语二/英语一/英语）、套题分组、状态与来源。
- 完形/阅读作答、判分、正确/错误高亮；完形原文空格回填。
- 段落翻译/划线句子翻译输入、参考译文、自评。
- Part B 无选项时只读原文并说明。
- 记录写入 Room 与 version 3 `papers`，支持同步与冷启动恢复。

## 验证证据

- JVM 单测：37/37 通过。
- 设备测试：6/6 通过（Room、Keystore、WorkManager、同步/认证、真题持久化）。
- M1 真机：游客/词卡评分/冷启动保留/拼写隐藏单词/句子草稿与自评/TTS 实际出声/专注倒计时。
- M2 真机：目录 44 套与筛选；2025 英语二 6 组；完形 20 题提交 `5/20`；
  阅读 5 题提交 `3/5` 并高亮；翻译参考与自评；2005 Part B 只读；
  记录在返回、冷启动与覆盖安装后保留。
- 修复：Manifest 缺少 `TTS_SERVICE` 查询导致系统 TTS 引擎不可见，已加入 `<queries>`。
- 无崩溃日志；TTS 修复后 AudioTrack 实际播放 15360 帧。

## 未完成 / 下次继续

1. **真实账号成功登录与跨端同步**：需要用户在 App 内登录一次；验证 token 写入、
   `/api/state` 拉取/上传、409 合并、离线重试。未创建测试账号。
2. **M3 收尾**：笔记、完整统计/热力图、备份导入导出、修改密码、平板双栏。
3. **发布**：生成 release keystore，替换 debug 签名；评估是否替换 `com.dailyenglish.app`。

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
