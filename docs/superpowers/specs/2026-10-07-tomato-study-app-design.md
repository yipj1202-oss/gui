# 晷（Guǐ）— 番茄学习法 App · 设计文档（第一版）

- App 名称：晷（英文备份名 Gui）
- Slogan：以晷量时，以行成事（App 启动 loading 页展示）
- 日期：2026-10-07
- 目标设备：仅用户自己的安卓手机，本地运行，无云
- 最低系统：Android 9.0 (API 28)
- 技术路线：原生 Kotlin + Jetpack Compose + Room，本地数据

详细 PRD 见仓库根目录 `PRD.md`，后续开发以 PRD.md 为准。

## 1. 目标

一个只在自己安卓手机上运行的番茄钟学习 App。核心场景：
1. 管理学习和日常任务（可带图片）
2. 选定某个任务后按自定义番茄方案开始专注
3. 事后按"任务分类"和"一天中哪个时段"查看统计
4. 桌面小组件随时瞄一眼当前进度和今日番茄数
5. 换机/留底时一键导出整个 App 数据（含图片）
6. 中英界面，两套预设主题（浅/深）

## 2. 明确不做的（第一版砍掉，以后再说）

- 云同步 / 多设备 / 账号
- CSV 导出、报告图片、PDF 报告
- 小组件内点按钮操作（只读显示）
- 自定义配色/圆角/字体（只给 2 套预设主题：浅色、深色）
- 打卡 streak、周对比、目标番茄数、长休息复杂排程引擎之外的功能
- 多语言除中/英以外

## 3. 模块与功能

### 3.1 番茄钟核心
- 默认方案：专注 25 分钟 + 休息 5 分钟，每完成 4 个专注后插入 15 分钟长休息
- 用户可保存多套"番茄方案"：每套可自定义专注时长、短休息时长、长休息间隔数（每 N 个后长休）、长休息时长
- 开始专注前可选一个任务（也可不选，标记为"无任务"）
- 计时用"开始时间戳 + 总时长"实现，不逐秒累加；App 被杀后重新打开按时间戳重算，进度不丢
- 后台/锁屏：前台 Service + 常驻通知显示剩余时间；支持"专注中/休息中"两种状态
- 结束提醒：本地通知 + 震动；休息结束提示开始下一段
- 手动跳过/提前结束当前段；每个完成的专注段记一条数据（时间戳、时长、关联任务 id）

### 3.2 任务/事件列表（TodoList + 图片）
- 增删改任务；任务可勾完成；可带 0..n 张图片（拍照或选相册，图片存 App 私有目录）
- 任务字段：标题、备注、图片、创建时间、完成时间
- 可从任务详情直接"开始番茄"（自动带任务 id 进计时）
- 列表按"进行中/已完成"分组，简单排序（按更新时间）

### 3.3 数据统计
只保留两类视图（对应需求 B + C）：
- 按任务分类：某时间范围（今天/本周/本月/自定义）内各任务的专注总时长与番茄数，条形图
- 一天时段分布：专注小时数按 24 小时（或 1-12 小时粒度）堆积柱状图，看哪个时段高产
- 数据全部来自已完成的专注段记录，无需额外埋点

### 3.4 桌面小组件
- 原生 AppWidget，规格 4x1
- 显示：当前状态（专注中/休息中/空闲）、剩余时间、当前任务名（若有）、今日已完成番茄数
- 纯只读；点击小组件打开 App 主页

### 3.5 数据备份（入口在设置页）
- 导出 / 导入功能入口放在「设置」页里，主页不放
- 导出：把数据库 + 图片目录打成单个 .guiback 文件（本质 zip）存到手机可见目录（下载/文件管理器）
- 导入：在设置里选文件恢复，覆盖前需确认
### 3.6 主题皮肤
- 两套预设：浅色（"晨间"）、深色（"夜间"）
- 设置里一键切换；数据和小组件同步跟随
- 预留 Theme 接口，以后加第三套只加资源不改代码

### 3.7 设置
- 语言：中 / 英
- 主题：浅 / 深
- 默认番茄方案（可指定某套）
- 通知与震动开关
- 数据：导出 / 导入 入口

## 4. 架构与目录

App 模块划分（Kotlin, Compose, Room）：

```
app/
  MainActivity.kt (单 Activity, Compose NavHost)
  data/
    Room: AppDatabase.kt, 实体 + DAO
    entities: Task.kt, TaskImage.kt, PomodoroSession.kt, PomodoroPreset.kt
    dao: TaskDao, SessionDao, PresetDao
    BackupStore.kt (导出/导入 zip)
  ui/
    screens: SplashScreen(loading, 显示 晷 + slogan), HomeScreen, TimerScreen, TaskListScreen, StatsScreen, SettingsScreen, PresetEditScreen
    theme: LightTheme, DarkTheme, Theme.kt
    widgets: 复用组件 (TaskCard, ProgressRing, BarChartView)
  service/
    TimerForegroundService.kt (前台 Service + 通知)
    PomodoroEngine.kt (状态机: idle->focus->break->focus..., 基于时间戳)
  widget/
    TomodoroWidget.kt (AppWidgetProvider, 4x1)
  util/
    TimeStamp.kt, ZipBackup.kt
```

## 5. 数据模型（简化）

- Task(id, title, note, createdAt, completedAt, completed)
- TaskImage(id, taskId, filePath)
- PomodoroPreset(id, name, focusMin, shortBreakMin, longBreakEvery, longBreakMin, isDefault)
- PomodoroSession(id, presetId, taskId nullable, startedAt, endedAt, type[focus/break/longBreak], plannedMin, completedMin, finishedByTime)

统计全部由 PomodoroSession 聚合得出，不加冗余表。

## 6. 关键行为与边界

- 小组件数据源：读 PomodoroEngine 的当前状态 + 查询今日已完成的 focus 段数
- 时间戳恢复：App 冷启动时若 Service 之前存活过的 session 尚未写入 completed，按 startedAt + plannedMin 判定该段是否"已自然结束"，按 finishedByTime 收尾并入库
- 图片存储：App 内部文件，包名沙盒内；导出时一并打包；导入时解压到内部目录
- 导出格式：zip 内含 `db.sqlite`、`images/`、`meta.json`(版本/时间戳)；导入校验版本号
- 语言：Compose + resources 双语；默认系统语言，可手动锁

## 7. 测试策略（第一版）

- 单元：PomodoroEngine 状态机、时间戳恢复、统计聚合 SQL
- 集成：Room DAO CRUD、备份 zip 打包/解压
- 手动：小组件显示、前台 Service 锁屏表现、切主题

## 8. 后续迭代（不在本版）

- 皮肤数量扩展、自定义配色
- CSV/报告图片导出
- 目标番茄数、打卡 streak
- 云备份（用户明确要求暂不做）

## 9. 安装与交付

- 由开发侧安装 Android Studio + SDK（需用户一次授权）
- 交付：可安装 .apk 放到 `app/build/outputs/apk/debug/`，用户手机 `adb install` 或拷贝安装
- 项目目录：`/Users/yip/Documents/开发软件尝试`

