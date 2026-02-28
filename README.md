# 缭乱 (LiaoLuan) — 新野兽派极简效率工具

> **「拒绝精致的伪装，直面生活的粗粝。」**

**缭乱 (LiaoLuan)** 是一款采用 **Neo-Brutalism (新野兽派)** 设计风格的 Android 个人效率应用。它摒弃了传统 Material Design 的圆润与柔和，用大胆的黑白高对比度、粗犷的边框和非传统的布局，为你提供一个最纯粹的任务、习惯与灵感管理平台。

---

## ✨ 核心功能

### 📋 任务清单 (Tasks)
- **快速创建与编辑** — 极速录入任务名称与详细描述
- **三级优先级视觉系统** — 低 / 中 / 高优先级，高饱和度色彩冲击
- **场景化标签** — 工作、生活、紧急、学习四大核心场景标签筛选
- **精确截止时间 + 动态倒计时** — 实时显示剩余时间，营造紧迫感
- **自定义提醒语** — 在通知栏用个性化文字鞭策自己
- **智能归档** — 左滑完成，自动归档保持列表清爽

### 🔥 习惯养成 (Habits)
- **灵活排程** — 自定义执行频率（周一/三/五等）和时间窗口
- **量化目标** — 设定每日目标值、单位和单次步进值
- **双模式支持** — 普通打卡模式 & 时长专注模式（含前台计时服务）
- **间隔循环提醒** — 在时间窗口内每隔 N 分钟循环提醒
- **连胜 (Streak) 统计** — 连续坚持天数实时展示
- **习惯日历 + 历史回溯** — 周视图日历，支持查看任意日期的完成情况
- **热力图统计** — 月度习惯完成数据可视化

### 📝 灵感笔记 (Notes)
- **极简编辑器** — 无干扰纯文本编辑
- **图文混排** — 支持插入图片，自动压缩
- **情绪标记** — 每条笔记可标记当时心境
- **置顶与折叠** — 重要笔记置顶，列表智能预览

### 💾 数据安全
- **全量 JSON 备份与恢复** — 支持任务、习惯、习惯历史、笔记的完整导入导出
- **事务保护** — 数据导入采用 `@Transaction` 原子操作，杜绝中途崩溃导致数据丢失
- **崩溃日志持久化** — 本地保存最近 5 次崩溃栈，便于问题排查

---

## 🎨 设计哲学：Neo-Brutalism

| 原则 | 实现 |
|------|------|
| **高对比度** | 纯黑 `#000000` + 纯白 `#FFFFFF` 为主基调，辅以高饱和度的蓝/黄/红 |
| **粗轮廓** | 所有 UI 元素带有 2dp-4dp 纯黑边框，拒绝模糊阴影 |
| **人文字体** | 集成瘦身版 **LXGW WenKai (霞鹜文楷)**，粗犷中注入人文气息 |
| **直角硬阴影** | 黑色实体色块作为阴影，立体剪纸感 |

---

## 🛠️ 技术架构

本项目采用 **100% Kotlin** 编写，遵循现代 Android 工程最高标准。

| 层级 | 技术选型 |
|------|----------|
| **UI 框架** | Jetpack Compose (Material3) |
| **架构模式** | 单 Activity + MVI (Model-View-Intent) |
| **依赖注入** | Dagger Hilt |
| **本地存储** | Room Database (SQLite) — 含复合索引优化 |
| **后台任务** | WorkManager + AlarmManager (精确闹钟) |
| **异步编程** | Kotlin Coroutines & StateFlow |
| **图片加载** | Coil |
| **序列化** | Kotlinx Serialization |

### 项目模块结构

```
liaoluan/
├── app/                    # 主模块 — Activity, ViewModel, Service, Receiver, Worker
├── core/
│   ├── database/           # Room 数据库 — Entity, DAO, Repository
│   └── designsystem/       # 共享 UI — Theme, Colors, Typography, DateHandle
└── feature/
    ├── habits/             # 习惯管理界面
    ├── notes/              # 笔记管理界面
    ├── stats/              # 统计热力图界面
    └── tasks/              # 任务管理界面
```

---

## 🏗️ 构建指南

1. 确保安装 **Android Studio Ladybug** 或更高版本
2. 克隆本项目：
   ```bash
   git clone https://github.com/luzzr-123/liaoluan.git
   ```
3. 等待 Gradle Sync 完成
4. 连接 Android 调试设备或启动模拟器 (minSdk 26 / Android 8.0+)
5. 点击 **Run 'app'**

---

## 📋 更新日志

### v1.1 — 深度优化与 Bug 修复 (2026-03-01)

**🔴 致命级修复**
- 消除 `runBlocking` 阻塞主线程导致的 ANR 风险
- BroadcastReceiver 协程生命周期保护 (`goAsync()`)
- 全量数据导入增加 `@Transaction` 事务保护，杜绝数据清零
- 全局异常捕获增加崩溃日志本地持久化
- **修复 Tab 跨页跳转卡死 Bug** — 使用 `snapshotFlow { settledPage }` 消除竞态

**🟠 高危级修复**
- 计时器改用 `SystemClock.elapsedRealtime()`，抵御系统时间篡改
- PendingIntent requestCode 溢出碰撞修复
- 通知渠道不再冷启动重建，保留用户自定义偏好
- 移除违反 Play Store 政策的电池优化 API

**🟡 中等级修复**
- Room 数据库复合索引优化 (习惯日志、任务、习惯表)
- 清除冗余 SDK 版本判断 (minSdk=26)
- `onBackPressed` 迁移至 `OnBackPressedDispatcher`
- `DateHandle` 固定 `Locale.US` 防止非公历日历解析异常
- JSON 导入 catch 补充日志记录

**🟢 低级修复**
- 删除重复 `ksp(libs.hilt.compiler)` 声明
- 清理重复注释
- 数据库版本升级 v8 → v9

### v1.0 — 初始发布
- 任务管理、习惯追踪、灵感笔记三大核心模块
- 习惯日历与历史回溯
- 全量 JSON 备份与恢复
- Neo-Brutalism 设计系统

---

## 📄 许可证

本项目为个人项目。

---

*Created with ❤️ & Brutality by Luuzr*
