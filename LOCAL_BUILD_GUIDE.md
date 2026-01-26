# 本地构建测试指南

## 📦 快速开始

### 方法零：Android Studio 打包（推荐新手）

**适用场景**：使用 Android Studio 进行开发，图形界面操作

#### 1️⃣ 导入项目

1. **打开 Android Studio**
2. **选择 Open**（或 File → Open）
3. **选择项目的 `android` 目录**
   ```
   C:\Users\EDY\CodeBuddy\20260126090212\android
   ```
4. **等待 Gradle 同步完成**（首次可能需要 3-10 分钟下载依赖）

#### 2️⃣ 构建 Debug APK

**图形界面方式（推荐）**：
1. 菜单栏：**Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. 等待构建完成（右下角会显示进度）
3. 构建成功后，点击弹出通知中的 **locate** 链接
4. APK 文件位置：`android\app\build\outputs\apk\debug\app-debug.apk`

**Gradle 面板方式**：
1. 右侧打开 **Gradle** 面板（或 View → Tool Windows → Gradle）
2. 展开：**app** → **Tasks** → **build**
3. 双击 **assembleDebug** 开始构建
4. 构建成功后，在 `app\build\outputs\apk\debug\` 目录找到 APK

#### 3️⃣ 构建 Release APK（需要签名）

1. 菜单栏：**Build** → **Generate Signed Bundle / APK**
2. 选择 **APK** → Next
3. 如果没有签名文件：
   - 点击 **Create new...**
   - 填写密钥库路径、密码、别名等信息
   - 保存为 `app-organizer-key.jks`
4. 选择构建类型：**release**
5. 点击 **Finish** 开始构建

#### 4️⃣ 直接在真机/模拟器上运行

1. **连接手机**（USB 调试）或**启动模拟器**
2. 点击工具栏的 **绿色运行按钮**（或 Shift+F10）
3. 选择设备
4. 自动构建并安装到设备

#### 5️⃣ 查看构建日志

- **Build 窗口**：View → Tool Windows → Build
- **查看详细错误**：点击具体的错误行查看堆栈跟踪

---

### 方法一：命令行构建（快速）

**适用场景**：命令行快速构建，没有 ADB，手动传输 APK 到手机

```batch
# 双击运行
build_local.bat
```

> 💡 **对比**：Android Studio 图形界面更直观，批处理脚本更快速

**步骤**：
1. 运行脚本，等待构建完成（约 1-3 分钟）
2. APK 会自动复制到 `build_output\App-Organizer-debug.apk`
3. 通过微信、QQ、USB、云盘等方式传输到手机
4. 在手机上点击安装测试

---

### 方法二：命令行 + USB 直接安装（最快）

**适用场景**：手机通过 USB 连接电脑，已安装 ADB

```batch
# 双击运行
build_and_install.bat
```

**前提条件**：
1. 安装 Android SDK Platform Tools（包含 ADB）
   - 下载：https://developer.android.com/studio/releases/platform-tools
   - 解压后添加到系统 PATH
2. 手机开启开发者选项和 USB 调试
3. 用 USB 连接手机，允许调试授权

**步骤**：
1. 连接手机并允许 USB 调试
2. 运行脚本
3. 自动构建、卸载旧版、安装新版
4. 直接在手机上测试

---

### 方法三：快速测试菜单

```batch
# 双击运行，提供多种选项
quick_test.bat
```

---

## 🎨 Android Studio 开发技巧

### 实时预览布局

1. 打开 XML 布局文件（如 `activity_main.xml`）
2. 右上角切换到 **Split** 或 **Design** 模式
3. 实时查看界面效果

### 快捷键（Windows）

| 操作 | 快捷键 |
|------|--------|
| 运行应用 | Shift+F10 |
| 构建项目 | Ctrl+F9 |
| 查找文件 | Ctrl+Shift+N |
| 查找类 | Ctrl+N |
| 全局搜索 | Ctrl+Shift+F |
| 代码格式化 | Ctrl+Alt+L |
| 优化导入 | Ctrl+Alt+O |

### Logcat 调试

1. **运行应用后**，底部打开 **Logcat** 面板
2. **过滤日志**：在搜索框输入 `package:com.apporganizer`
3. **查看崩溃信息**：搜索 `AndroidRuntime` 或 `FATAL`

### 解决常见同步问题

**Gradle 同步失败**：
```
File → Invalidate Caches / Restart → Invalidate and Restart
```

**依赖下载慢**：
- 项目已配置阿里云镜像，正常情况下会自动使用
- 检查网络连接

**模拟器无法启动**：
- Tools → AVD Manager → 创建新设备
- 推荐：Pixel 5 + Android 11

---

## 🔧 手动构建命令

如果你熟悉命令行，可以直接执行：

```batch
# 进入 android 目录
cd android

# 构建 Debug 版本
gradlew.bat assembleDebug

# 构建 Release 版本（需要签名）
gradlew.bat assembleRelease

# 清理构建
gradlew.bat clean
```

**输出位置**：
- Debug APK: `android\app\build\outputs\apk\debug\app-debug.apk`
- Release APK: `android\app\build\outputs\apk\release\app-release.apk`

---

## 📱 ADB 安装步骤

### 安装 Platform Tools

1. **下载**：https://developer.android.com/studio/releases/platform-tools
2. **解压**到任意目录，如 `C:\Android\platform-tools`
3. **添加到 PATH**：
   - 右键"此电脑" → 属性 → 高级系统设置
   - 环境变量 → 系统变量 → Path → 编辑
   - 新建 → 输入 `C:\Android\platform-tools`
   - 确定保存

### 开启手机 USB 调试

1. **开启开发者选项**：
   - 设置 → 关于手机 → 连续点击"版本号"7次
   
2. **开启 USB 调试**：
   - 设置 → 系统 → 开发者选项 → USB 调试（开启）
   
3. **连接电脑**：
   - 用 USB 线连接
   - 手机会弹窗询问是否允许调试，点击"允许"

4. **验证连接**：
   ```batch
   adb devices
   ```
   应该显示你的设备序列号

---

## 🔄 开发测试流程

### 推荐工作流

1. **修改代码**
   ```
   修改 MainActivity.kt / 布局文件 等
   ```

2. **本地测试**
   ```batch
   build_and_install.bat
   # 或
   build_local.bat
   ```

3. **确认功能正常**
   ```
   在手机上测试各项功能
   ```

4. **提交到 GitHub**
   ```batch
   git add -A
   git commit -m "功能描述"
   git push
   ```

5. **CI/CD 自动构建**
   ```
   GitHub Actions 自动构建并发布到 Release
   ```

---

## ⚡ 提升构建速度

### 首次构建慢？

首次构建需要下载依赖，可能需要 5-10 分钟。后续构建会快很多（30秒-2分钟）。

### 使用 Gradle 缓存

已自动配置，无需额外设置。

### 仅构建变更部分

```batch
# 增量构建（推荐）
cd android
gradlew.bat assembleDebug

# 完整清理构建（慢，仅在出错时使用）
gradlew.bat clean assembleDebug
```

---

## 🐛 常见问题

### 1. Gradle 下载慢

**解决**：使用镜像源（已在项目中配置阿里云镜像）

### 2. ADB 无法识别设备

**检查**：
- USB 线是否支持数据传输（不是只充电线）
- 手机是否允许 USB 调试授权
- 更换 USB 口
- 重启 ADB：`adb kill-server && adb start-server`

### 3. 安装失败 INSTALL_FAILED_UPDATE_INCOMPATIBLE

**原因**：旧版本签名不同

**解决**：先卸载旧版
```batch
adb uninstall com.apporganizer
```

### 4. 构建失败

**解决**：
```batch
cd android
gradlew.bat clean
gradlew.bat assembleDebug --stacktrace
```
查看详细错误信息

---

## 📊 构建时间对比

| 方式 | 构建时间 | 安装时间 | 总耗时 | 适用场景 |
|------|---------|---------|--------|---------|
| Android Studio 构建 | 1-2分钟 | - | ~1-2分钟 | 开发调试，实时预览 |
| Android Studio 直接运行 | 1-2分钟 | 5-10秒 | ~1-2分钟 | 快速迭代测试 |
| 命令行 + 手动传输 | 1-2分钟 | 10-30秒 | ~2-3分钟 | 无 USB 连接 |
| ADB 直接安装 | 1-2分钟 | 5-10秒 | ~1-2分钟 | 自动化测试 |
| GitHub CI/CD | 3-5分钟 | 下载30秒 | ~4-6分钟 | 正式发布 |

**结论**：
- **开发阶段**：Android Studio 直接运行（最方便）
- **快速测试**：命令行脚本（最快速）
- **正式发布**：GitHub CI/CD（最可靠）

---

## 💡 最佳实践

### 针对不同场景

1. **首次开发/学习**：
   - 使用 Android Studio 导入项目
   - 熟悉图形界面操作
   - 利用代码提示和错误检查

2. **日常开发**：
   - Android Studio 直接运行到真机/模拟器
   - 修改代码 → Shift+F10 → 立即查看效果
   - 使用 Logcat 调试

3. **快速验证**：
   - 小改动：命令行脚本快速打包
   - 大功能：Android Studio 完整测试

4. **发布版本**：
   - 本地测试确认无误
   - 提交到 GitHub
   - CI/CD 自动构建正式版

### 推荐工作流

```
修改代码
   ↓
Android Studio 直接运行（Shift+F10）
   ↓
在真机/模拟器上测试
   ↓
功能正常？
   ├─ 否 → 查看 Logcat 日志 → 修复 bug
   └─ 是 → 提交到 GitHub
```

---

## 🎯 一句话总结

- **新手推荐**：Android Studio 图形界面，直观易用 🎨
- **开发推荐**：Android Studio 直接运行，即时反馈 ⚡
- **快速打包**：命令行脚本，2分钟出包 🚀
- **正式发布**：GitHub CI/CD，自动化部署 ✅

**先用 Android Studio 熟悉项目，再用命令行提升效率！**
