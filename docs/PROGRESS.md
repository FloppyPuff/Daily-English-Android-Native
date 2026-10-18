# Native Android 进度存档

更新时间：2026-10-18 21:45（CST）  
状态：**M1、M2、M3 完成并已真机验证；1.0.0 已交付。**

## 仓库与交付

- 工程：`native-android/`（独立 git 仓库，当前分支 `main`）
- 标签：`v1.0.0-m3`
- APK：`app/build/outputs/apk/debug/app-debug.apk`
- 桌面副本：`C:\Users\fjh28\Desktop\Daily-English-Native-1.0.0-debug.apk`
- SHA-256：`c021d280f023be2e9a07aa5fe74102c5bd0e76833333841405f110cb23461e12`
- 设备：PMW110 / Android 17（API 37）；APK 1.0.0 已安装。

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

## 已完成（M3 完整对等）

- 笔记：收藏词/句、薄弱 & 错题（含真题错词）、搜索、进入练习、宽屏双栏。
- 记录：累计已学/较熟悉/学习天数/专注分钟/真题、7 日节奏、掌握比例、12 周热力图。
- 备份：完整 version 3 JSON 导出/恢复、5 MB 上限、二次确认、失败不覆盖。
- 修改密码：表单校验 + `/api/password`，成功后提示其他设备重新登录。
- 自适应：`>=600dp` NavigationRail（短屏图标模式）；笔记/记录 `>=720dp` 双栏。
- 动态取色：Android 12+ 跟随壁纸，默认关闭；偏好仅存本机 DataStore。

## 验证证据

- JVM 单测：49/49 通过。
- 设备测试：7/7 通过（Room、Keystore、WorkManager、同步/认证、真题持久化、导入替换）。
- M1 真机：游客/词卡评分/冷启动保留/拼写隐藏单词/句子草稿与自评/TTS 实际出声/专注倒计时。
- M2 真机：目录 44 套与筛选；2025 英语二 6 组；完形 20 题提交 `5/20`；
  阅读 5 题提交 `3/5` 并高亮；翻译参考与自评；2005 Part B 只读；
  记录在返回、冷启动与覆盖安装后保留。
- M3 真机：笔记收藏与进入练习；统计页真实数据；备份导出 5,100 B 并通过网页
  `parseBackup`；密码表单校验；横屏 NavigationRail；深浅色与动态取色切换。
- 真实账号登录与跨端同步已由用户验证。
- 修复：Manifest 缺少 `TTS_SERVICE` 查询导致系统 TTS 引擎不可见，已加入 `<queries>`。

## 未完成 / 下次继续

1. **发布**：生成 release keystore，替换 debug 签名；评估是否替换 `com.dailyenglish.app`。
2. **设备覆盖**：物理平板/折叠屏双栏与系统字体缩放的实机验收。
3. **备份 UI**：系统文件选择器未做自动化点击；格式/校验/导入替换已由测试覆盖。

## 恢复命令

```powershell
$env:JAVA_HOME = "D:\Software\Tool\AndroidStudio\jbr"
cd native-android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

设备测试：

```powershell
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.dailyenglish.native.test/androidx.test.runner.AndroidJUnitRunner
```

`connectedDebugAndroidTest` 会在结束后卸载 App；设备已登录时优先使用上面的
`am instrument` 方式。`SessionStoreTest` 现已备份并恢复原有会话。

## 注意

- 不要在 `android-app/`（Capacitor 版）与 `native-android/` 之间复制 token 或数据库；
  两端只通过账号同步。
- 内容更新：修改 `../web/data` 后重新构建，`syncContent` 任务会复制到 assets。
- IP 证书与后端入口由服务器维护；App 端只依赖 `API_BASE_URL`。
- 本次 M3 测试运行过旧版 `SessionStoreTest`，会清除本机登录 token；App 当前停在
  认证页，重新登录即可，账号与服务器数据不受影响。
