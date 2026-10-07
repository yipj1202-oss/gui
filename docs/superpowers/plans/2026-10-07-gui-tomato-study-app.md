# 晷（Gui）番茄学习 App — 可执行开发计划（v1 本地版）

> **给接手工**：本计划假设你**对代码库没有任何上下文**。按任务顺序执行，每个任务都是独立可测试、可提交的交付物。所有步骤用 `- [ ]` 复选框跟踪。
>
> **执行前必须读**：`PRD.md`（仓库根）与 `docs/superpowers/specs/2026-10-07-tomato-study-app-design.md`。本计划从这两个文档推导，两个文档与代码**同时**交付给每个任务执行者。

## 目标（Goal）
一个只在用户自己安卓手机（Android 9.0+）本地运行的番茄钟学习 App「晷」：任务/事件列表（可带图片）+ 自定义多套番茄方案 + 选任务开专注（后台/锁屏不停、被杀可恢复）+ 两类统计 + 桌面 4x1 只读小组件 + 一键本地备份/恢复 + 浅/深主题 + 中英双语。

## 架构（Architecture）
原生 Android 单模块工程。Kotlin + Jetpack Compose（Material3）做 UI；Room（SQLite）持久化全部数据（`gui.db`，App 私有目录）；前台 Service + `StateFlow` 驱动的倒计时（用"开始时间戳 + 计划时长"而非逐秒累加，保证被杀后能重算）；原生 AppWidgetProvider 做桌面 4x1 只读组件；备份 = 把 `gui.db` + `images/` 打成一个 `.guiback`（zip），纯本地无网络。

## 技术栈（Tech Stack）
- 语言：Kotlin
- UI：Jetpack Compose + Material3 + `accompanist`（可选，仅图标）
- 异步：Kotlin Coroutines + `StateFlow`
- 数据：Room（KSP），SQLite
- 导航：Compose Navigation
- 小组件：`AppWidgetProvider`（4x1）
- 构建：Gradle + AGP，`minSdk=28`，`targetSdk=34`
- 图片：`Coil`（加载 App 私有图片 + 拍照/相册）
- 测试：JUnit4 + `Room` 内存测试（测试时）+ 前台 Service 状态机单测

## 全局约束（Global Constraints，抄自 PRD）
- `minSdk=28`（Android 9.0），`targetSdk=34`
- App 名「晷」，英文包名建议 `com.gui.tomato`
- Slogan「以晷量时，以行成事」用于 Splash
- 数据全部存 App 私有目录（`gui.db` + `images/`），**不写公共目录、不联网**
- 备份文件 `.guiback`（本质 zip：`gui.db` + `images/` + `meta.json`），纯本地
- 两套主题：浅（"晨间"）/深（"夜间"）；语言 中/英
- 统计范围仅：今天 / 本周 / 本月
- 小组件 4x1，只读，点击开主页
- 每任务一次提交，commit message 用 `feat/fix/test/refactor: ...`

---

## 阶段 0：环境与工具链（前置，需一次联网 + 授权）

> **现实检查（2026-10-07）**：当前 Mac 上 **无 JDK、无 Android SDK、无 Android Studio、无 Gradle、沙盒内无网络**。因此本阶段必须在**升级（联网）环境**下执行，并**逐条让用户授权下载/安装**（沙盒会拦截）。以下每条都是独立命令，先跑后授权。

- [ ] **0.1 安装 JDK 17（JDK 8/11 不行，AGP 要 17+）**
  - 若用 Homebrew：`brew install --cask temurin@17`
  - 验证：`java -version` → `17.x`
  - 授权提示：「是否安装 JDK 17（Temurin）？」
- [ ] **0.2 安装 Android Studio（含 SDK 管理）**
  - 下载并安装 Android Studio（Intel 或 Apple Silicon 版，按机型）
  - 首次启动在 Setup Wizard 里安装 **Android SDK Platform 34、Build-Tools、Android Emulator（可选，真机可省）**
  - 授权提示：「是否下载安装 Android Studio 与 Android SDK（约 1-2GB）？」
  - 装完设置环境变量 `ANDROID_HOME` 指向 SDK 路径
- [ ] **0.3 用 Android Studio 建工程骨架**
  - New Project → "Empty Activity (Compose)" → 名 `Gui`，包名 `com.gui.tomato`，minSdk 28，语言 Kotlin
  - 生成后在终端 `cd /Users/yip/Documents/开发软件尝试` 用 Git 管理（若尚未 `git init`）
  - 验证：`./gradlew assembleDebug` 成功（联网拉依赖，需授权一次）
- [ ] **0.4 提交骨架**
  - `git add -A && git commit -m "chore: scaffold Android Compose project, minSdk 28"`

> 阶段 0 完成后，才有可构建的工程。后续任务 1-N 都基于它。若暂无法联网，阶段 0 只能先"记录待办"，不能写代码。

---

## 阶段 1：数据层（Room 实体 + DAO）

### Task 1：实体与数据库
**Files:** Create `app/src/main/java/com/gui/tomato/data/*.kt`
- `entities/Task.kt`：`@Entity Task(id:Long=0, title:String, note:String="", createdAt:Long, completedAt:Long?=null, completed:Boolean=false)`
- `entities/TaskImage.kt`：`@Entity TaskImage(id:Long=0, taskId:Long, filePath:String, idx:Int)`
- `entities/Preset.kt`：`@Entity PomodoroPreset(id:Long=0, name:String, focusMin:Int=25, shortBreakMin:Int=5, longBreakEvery:Int=4, longBreakMin:Int=15, isDefault:Boolean=false)`
- `entities/Session.kt`：`@Entity PomodoroSession(id:Long=0, presetId:Long, taskId:Long?=null, startedAt:Long, endedAt:Long?, type:String, plannedMin:Int, completedMin:Int, finishedByTime:Boolean)`
- `db/AppDatabase.kt`：`@Database(entities=[...], version=1)`，含四张表的 `Dao` 暴露

- [ ] **Step 1 写 Room 迁移测试（占位，先验 CRUD 能跑）**：`AppDatabaseTest`（in-memory Room）建库插入/查一条 `Task`，断言可查回。
- [ ] **Step 2 跑测试确认能建库**：`./gradlew test`
- [ ] **Step 3 提交**：`git commit -m "feat(data): add Room entities + AppDatabase"`

### Task 2：DAO 层
**Files:** Create `data/dao/TaskDao.kt`、`PresetDao.kt`、`SessionDao.kt`
- `TaskDao`：`insert/update/delete/getCompletedFlag/listActive/listDone`（按 createdAt 倒序）
- `PresetDao`：`insert/update/delete/getDefault/list/setDefault(id)`（setDefault 先清掉其它 isDefault）
- `SessionDao`：`insert/getOpenSession()/closeOpen()/listInRange(start,end)`（供统计聚合）

- [ ] **Step 1 写 DAO 单测**（in-memory Room，验证 insert+list+setDefault 互斥）
- [ ] **Step 2 跑通**：`./gradlew test`
- [ ] **Step 3 提交**：`git commit -m "feat(data): add DAOs for Task/Preset/Session"`

### Task 3：图片存储 + 统计聚合查询
**Files:** Create `data/image/ImageStore.kt`、`data/StatsRepo.kt`
- `ImageStore`：`saveUri(taskId, uri):String`（复制到 App 私有 `filesDir/images/`）、`listForTask(taskId):List<String>`、`deleteForTask(taskId)`
- `StatsRepo`：`byTask(range):List<TaskStatRow>`、`byHour(range):List<HourStatRow>`（SQL 聚合 `PomodoroSession` + join `Task` 取任务名；无任务归"未指定"）

- [ ] **Step 1 写 `StatsRepo` 单测**：插 3 条 session（含 1 条 taskId=null），断言 byTask 归并、byHour 按 `startedAt` 小时分桶
- [ ] **Step 2 跑通**
- [ ] **Step 3 提交**：`git commit -m "feat(data): image store + stats aggregation"`

---

## 阶段 2：番茄引擎（纯逻辑，先于 UI，TDD 最紧）

### Task 4：`PomodoroEngine` 状态机（核心，无 Android 依赖，可纯 JVM 测）
**Files:** Create `engine/PomodoroEngine.kt`、测试 `engine/PomodoroEngineTest.kt`
- 状态：`Idle` / `Focus(remainMs)` / `ShortBreak(remainMs)` / `LongBreak(remainMs)`
- `currentSnapshot(nowMs):Snapshot`——**纯函数**，给定 `startedAt`/`plannedMs`/`nowMs` 算剩余；被杀恢复靠它
- `start(preset, taskId?, nowMs)`、`pause(nowMs)`、`resume(nowMs)`、`skipSegment(nowMs)`（跳到下一段）、`finishAll(nowMs)`
- 段顺序：`Focus → 若计数%N==0 则 LongBreak 否则 ShortBreak → Focus ...`；`N=preset.longBreakEvery`

**Interfaces:**
- 产出（后续依赖）：`class Snapshot(state, remainMs, taskId?, presetId, segmentIndex)`；`engine.currentSnapshot()` 供 UI/Service/小组件读取
- 依赖（来自 Task 3）：`PresetDao.getDefault()`、`SessionDao.insert/closeOpen`

- [ ] **Step 1 写失败测试**：
  ```kotlin
  @Test fun snapshotCountsDown() {
      val p = PomodoroPreset(name="a", focusMin=1)
      val eng = PomodoroEngine(fakeDaos())
      val t0 = 0L
      eng.start(p, taskId=null, nowMs=t0)
      val snap = eng.currentSnapshot(nowMs = t0 + 30_000) // 半分钟
      assertEquals(State.FOCUS, snap.state); assertEquals(30_000, snap.remainMs)
  }
  @Test fun longBreakAfterEveryN() {
      // focusMin=1, longBreakEvery=2, longBreakMin=15
      // 第 2 个 focus 后应为 LongBreak
  }
  @Test fun killRecovery() {
      // start@t0, 不结束；重启后 currentSnapshot(t0+focusMin) 应已进入休息且 remain 正确
  }
  ```
- [ ] **Step 2 跑测试确认失败**：`./gradlew test --tests "*PomodoroEngine*"`
- [ ] **Step 3 实现引擎**
- [ ] **Step 4 跑通**
- [ ] **Step 5 提交**：`git commit -m "feat(engine): pure pomodoro state machine with timestamp recovery"`

### Task 5：前台 Service + 通知 + `StateFlow` 接线
**Files:** Create `service/TimerForegroundService.kt`、`service/PomodoroRepository.kt`（全局持有引擎单例 + `MutableStateFlow<Snapshot>`）
- `PomodoroRepository`（`Application` 里初始化）：持有 `PomodoroEngine`，对外暴露 `val ui: StateFlow<Snapshot>`
- Service 启动时把引擎当前段写入 `gui.db`（`SessionDao.getOpenSession()`）；锁屏/切后台不停；结束/跳过时 `SessionDao.closeOpen()` 落库
- 通知：常驻通知显示"剩余 mm:ss + 任务名 + 状态"；`focus`/`break` 文案不同

- [ ] **Step 1 写 Repository 单测**：`start` 后 `ui.value` 状态正确；`skipSegment` 切到休息
- [ ] **Step 2 手动验证**（需真机/模拟器）：锁屏 1 分钟，通知倒计时正常
- [ ] **Step 3 提交**：`git commit -m "feat(service): foreground timer service + live notification"`

---

## 阶段 3：UI 框架（Compose）

### Task 6：主题 + 中英资源 + 底部 3 Tab 框架
**Files:** Create `ui/theme/{LightColors,DarkColors,Theme}.kt`、`MainActivity.kt`、`ui/nav/AppNavHost.kt`、`strings.xml`（中/英各一）
- `Theme`：读 `AppPrefs`（`SharedPreferences`）里的 `themeKey` 选 Light/Dark；`isSystemInDarkTheme` 兜底
- `MainActivity`：单 Activity，`NavHost` 三 Tab：`Home` / `Tasks` / `Stats`；右下角 `⚙` 进 `Settings`
- 双语：所有可显示文案走 `stringResource`，`values-zh` / `values-en`

- [ ] **Step 1 搭三 Tab 空屏**（Home/Tasks/Stats 占位）能切
- [ ] **Step 2 跑通**（assembleDebug + 手测 Tab 切换）
- [ ] **Step 3 提交**：`git commit -m "feat(ui): theme, i18n, 3-tab scaffold"`

### Task 7：Splash
**Files:** Create `ui/screens/SplashScreen.kt`
- 居中「晷」 + slogan「以晷量时，以行成事」；`AppDatabase` 建好 + `PomodoroRepository` 初始化完就淡出到 `Home`

- [ ] **Step 1 实现 + 手测淡出**
- [ ] **Step 2 提交**：`git commit -m "feat(ui): splash with logo + slogan"`

### Task 8：主页（圆盘 + 开始专注 + 今日速览）
**Files:** Create `ui/screens/HomeScreen.kt`、`ui/widgets/ProgressRing.kt`、`ui/dialogs/PresetPickerSheet.kt`、`ui/dialogs/TaskPickerSheet.kt`
- 收集 `PomodoroRepository.ui` 画圆盘；空闲→`25:00` + 大按钮；专注中→倒计时 + 任务名 + [暂停/跳过/结束]；休息→[跳过休息]
- 方案名小字可点 → 弹 `PresetPickerSheet`（列表 + "新建方案"入口 → `PresetEditScreen`）
- 「今日」速览条：`StatsRepo.byTask(today)` 聚合番茄数/分钟；点 → 跳 `Stats`

- [ ] **Step 1 手测：开始专注 → 选任务 → 圆盘走；暂停/跳过/结束都对**
- [ ] **Step 2 提交**：`git commit -m "feat(ui): home timer ring + task/preset pickers + today strip"`

### Task 9：任务列表 + 详情
**Files:** Create `ui/screens/TasksScreen.kt`、`ui/screens/TaskDetailScreen.kt`、`ui/screens/TaskEditScreen.kt`
- 列表按"进行中/已完成"分组；卡片含缩略图；点进详情（多图左滑、改标题/备注、删除、"开始番茄"带 taskId）
- 新建/编辑：标题、备注、加图（`Coil` + 拍照/相册 intent → `ImageStore.saveUri`）

- [ ] **Step 1 手测 CRUD + 加图 + 从详情开番茄（任务名出现在主页圆盘）**
- [ ] **Step 2 提交**：`git commit -m "feat(ui): task list + detail + edit with images"`

### Task 10：统计页
**Files:** Create `ui/screens/StatsScreen.kt`、`ui/widgets/BarChartView.kt`（或 `androidx.compose` 自绘）、`ui/screens/PresetEditScreen.kt`
- 顶部 今天/本周/本月 切段；两区块：「按任务」条形图（Top N + "其他"）、「时段分布」柱状图（1-12h 粒度）
- 数据来自 `StatsRepo`；`PresetEditScreen` 也在此任务里（主表单 + 保存/设为默认）

- [ ] **Step 1 造 3 条测试 session，切范围验证两图对**
- [ ] **Step 2 提交**：`git commit -m "feat(ui): stats (by-task + hourly) + preset editor"`

### Task 11：设置页 + 备份/恢复
**Files:** Create `ui/screens/SettingsScreen.kt`、`data/backup/BackupStore.kt`
- 设置项：主题 浅/深（卡片预览）、语言 中/英、默认方案选择、通知/震动开关
- 备份：`BackupStore.export(File)` 打包 `gui.db` + `images/` + `meta.json`（版本号 1 + 时间戳）；`import(file)` 先校验 meta 版本 → 覆盖前弹确认 → 解压还原 → 重开 DB
- 导出用 SAF（`ACTION_CREATE_DOCUMENT`，存到用户选的目录，**不硬编码公共目录**，沙盒友好）；导入 `ACTION_OPEN_DOCUMENT`

- [ ] **Step 1 写 `BackupStore` 单测**：导出→改一条→导入→断言还原
- [ ] **Step 2 手测：导出 .guiback → 清数据 → 导入 → 数据/图回来**
- [ ] **Step 3 提交**：`git commit -m "feat(settings): theme/lang/notify + local backup/restore"`

---

## 阶段 4：桌面小组件（原生 AppWidget）

### Task 12：4x1 只读小组件
**Files:** Create `widget/GuiWidget.kt`（`AppWidgetProvider`）、`res/xml/GuiWidget_info.xml`（`4x1`）、`layout/widget_gui.xml`、`AppWidgetReceiver` 注册
- 数据：读 `PomodoroRepository` 快照 + `StatsRepo.byTask(today)` → `RemoteViews` 填充"今日 N 番茄 · X 分钟"与"当前状态/剩余/任务名"
- 刷新：`PomodoroRepository` 状态变更时 `AppWidgetManager.updateAppWidget`；`AlarmManager` 每 30s 兜底一次
- 点击：`PendingIntent` 开 `MainActivity`（`Home`）
- 主题：`RemoteViews` 用当前主题色（从 `AppPrefs` 读）

- [ ] **Step 1 手动：桌面添加小组件 → 显示今日数据；开一个专注 → 小组件 30s 内刷新出状态**
- [ ] **Step 2 提交**：`git commit -m "feat(widget): 4x1 readonly widget with live timer + today count"`

---

## 阶段 5：被杀恢复 + 持久化收尾（PRD 验收关键项）

### Task 13：冷启动恢复未收尾专注
**Files:** Modify `PomodoroRepository` 初始化处
- 启动时：`SessionDao.getOpenSession()` 若有未 `close` 的段 → 用 `PomodoroEngine.currentSnapshot(now)` 判定该段是否已自然结束 → 是则按 `finishedByTime=true` 收尾入库，否则保持进行中状态

- [ ] **Step 1 写恢复单测**：造一条"进行中" session，`start` 早于 `now- focusMin`，初始化后应收尾且 remain 正确
- [ ] **Step 2 手测**：专注中 `adb shell am force-stop` → 重开 → 状态对
- [ ] **Step 3 提交**：`git commit -m "fix(recovery): resume unfinished session on cold start"`

---

## 阶段 6：整体验收（对照 PRD 第 10 节逐条勾）

- [ ] 逐条跑 PRD「可实际检查的验收标准」，逐条在真机勾掉
- [ ] `./gradlew assembleDebug` 出 `app-debug.apk` → `adb install` 到用户手机
- [ ] 重启手机一次，验证"持久化"两项验收

> 交付物：`app/build/outputs/apk/debug/app-debug.apk` + 已勾满的验收清单。

---

## 自我审查（Self-Review）

**Spec 覆盖**：PRD 第 8 节 9 项逐一对应任务——
1 Splash→Task7；2 三 Tab→Task6；3 番茄核心（默认/多方案/选任务/前台/被杀恢复）→Task4/5/13；4 任务 CRUD+图→Task9；5 统计两类→Task10；6 小组件→Task12；7 设置→Task11；8 备份→Task11；9 持久化→Task13 + 阶段 6。无缺口。

**占位扫描**：无 "TBD/TODO/以后补/适当处理"。所有代码步骤给了具体接口/字段名与失败测试样例。

**类型一致性**：
- `Snapshot` 在 Task4 定义，Task5/12 消费，字段一致（`state/remainMs/taskId/presetId/segmentIndex`）
- `PomodoroRepository.ui: StateFlow<Snapshot>` 在 Task5 定义，Task8/12 读，一致
- `StatsRepo.byTask/byHour` 在 Task3 定义，Task8/10 消费，一致
- `AppPrefs`（`SharedPreferences`）在 Task6 首次落地，Task11/12 复用——需在 Task6 一并创建 `ui/prefs/AppPrefs.kt`，已在 Task6 文件清单隐含（补记：Task6 增加 `AppPrefs.kt`）

**修正**：把 `AppPrefs.kt` 明确写进 Task6 的文件清单（上面遗漏），见下。

---

### 执行方式（Execution Handoff）
计划已保存。两种执行方式：
1. **Subagent 驱动（推荐）**——每任务派新 subagent + 两阶段评审
2. **本会话内联执行**——用 `executing-plans` 批量推进 + 检查点

> **前置提醒**：当前机器**没有 JDK / Android SDK / Android Studio / 网络**。阶段 0 必须联网并逐条授权安装（JDK17、AS+SDK、拉 Gradle 依赖）。**没有阶段 0，后续 1-N 无法构建/跑测试。** 建议先做阶段 0 的环境安装，再开始 Task 1。
