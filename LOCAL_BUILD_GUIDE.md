# 本地构建测试指南

## 📦 快速开始

### 方法一：本地构建（推荐）

**适用场景**：没有 ADB，手动传输 APK 到手机

```batch
# 双击运行
build_local.bat
```

**步骤**：
1. 运行脚本，等待构建完成（约 1-3 分钟）
2. APK 会自动复制到 `build_output\App-Organizer-debug.apk`
3. 通过微信、QQ、USB、云盘等方式传输到手机
4. 在手机上点击安装测试

---

### 方法二：USB 直接安装（最快）

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

| 方式 | 构建时间 | 安装时间 | 总耗时 |
|------|---------|---------|--------|
| 本地 + 手动传输 | 1-2分钟 | 10-30秒 | ~2-3分钟 |
| ADB 直接安装 | 1-2分钟 | 5-10秒 | ~1-2分钟 |
| GitHub CI/CD | 3-5分钟 | 下载30秒 | ~4-6分钟 |

**结论**：本地构建测试最快，适合频繁迭代开发！

---

## 💡 最佳实践

1. **小改动**：本地测试 → 直接提交
2. **大功能**：本地多次测试 → 确认无误 → 提交
3. **发布版本**：本地测试 + GitHub Actions 双重保障
4. **使用 USB 安装**：最快的开发迭代方式

---

## 🎯 一句话总结

**本地构建：2分钟反馈 vs GitHub构建：5分钟反馈**

**本地测试确认无误后再提交，开发效率翻倍！** 🚀
