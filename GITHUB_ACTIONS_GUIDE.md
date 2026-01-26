# 🚀 GitHub Actions 自动构建指南

## ✅ 我已经帮你配置好了！

现在只需要把代码推送到 GitHub，就能**自动构建 APK**！

---

## 📋 使用步骤

### 第 1 步：创建 GitHub 仓库

1. **访问 GitHub**
   - 登录 https://github.com
   - 点击右上角 "+" → "New repository"

2. **创建仓库**
   ```
   Repository name: app-organizer
   Description: Android 应用整理工具
   ☑️ Public (推荐) 或 Private
   ❌ 不要勾选 Initialize this repository with...
   点击 "Create repository"
   ```

### 第 2 步：推送代码到 GitHub

在项目根目录打开命令行（PowerShell 或 Git Bash），执行：

```bash
# 初始化 Git 仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: Android 应用整理工具"

# 连接到你的 GitHub 仓库（替换成你的用户名）
git remote add origin https://github.com/你的用户名/app-organizer.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

**注意**：把 `你的用户名` 替换成你的 GitHub 用户名！

### 第 3 步：等待自动构建

1. **推送后立即触发**
   - 代码推送后，GitHub Actions 会自动开始构建
   - 访问：`https://github.com/你的用户名/app-organizer/actions`

2. **查看构建进度**
   ```
   在 Actions 页面可以看到：
   ✅ 构建中 / 构建成功 / 构建失败
   
   点击具体的构建任务可以看详细日志
   ```

3. **构建时间**
   - 首次：约 5-8 分钟（需要下载依赖）
   - 后续：约 2-3 分钟（有缓存）

### 第 4 步：下载 APK

1. **进入 Actions 页面**
   - `https://github.com/你的用户名/app-organizer/actions`

2. **点击最新的构建任务**
   - 选择绿色✅的那个（构建成功）

3. **滚动到底部**
   - 找到 "Artifacts" 部分
   - 点击 "应用整理工具-APK" 下载

4. **解压并使用**
   - 下载的是 ZIP 文件
   - 解压后得到 `应用整理工具-debug.apk`
   - 传到手机安装即可

---

## 🎯 自动触发规则

GitHub Actions 会在以下情况自动构建：

| 触发条件 | 说明 |
|---------|------|
| 推送到 main/master 分支 | 每次 `git push` 自动构建 |
| 创建 Pull Request | 合并前自动测试 |
| 手动触发 | 在 Actions 页面点击 "Run workflow" |

---

## 📁 配置文件说明

### `.github/workflows/build-apk.yml`

这是 GitHub Actions 的配置文件，它会：

```yaml
✅ 1. 检出代码
✅ 2. 安装 JDK 17
✅ 3. 配置 Gradle 缓存（加速构建）
✅ 4. 执行 Gradle 构建
✅ 5. 重命名 APK 为中文名
✅ 6. 上传 APK（保留 30 天）
✅ 7. 显示 APK 信息
```

### `.gitignore`

已配置忽略文件：
- 构建产物（build/、*.apk）
- IDE 配置（.idea/、*.iml）
- 临时文件（.gradle/、.DS_Store）

---

## 🔧 高级配置（可选）

### 1. 构建 Release 版本

如果想构建正式版本，修改 `.github/workflows/build-apk.yml`：

```yaml
- name: 📦 构建 Release APK
  run: |
    cd android
    ./gradlew assembleRelease --stacktrace
```

**但需要先配置签名！** 参考下面的签名配置。

### 2. 配置自动签名

#### 步骤 A：生成密钥

在本地执行：
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-app
```

按提示输入密码和信息。

#### 步骤 B：上传密钥到 GitHub Secrets

1. **将密钥转换为 Base64**
   ```bash
   # Windows PowerShell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks")) | Out-File keystore.txt
   
   # Linux/Mac
   base64 release-key.jks > keystore.txt
   ```

2. **在 GitHub 仓库设置 Secrets**
   - 访问：`Settings → Secrets and variables → Actions`
   - 点击 "New repository secret"
   - 添加以下 Secrets：
     ```
     KEYSTORE_BASE64: （keystore.txt 的内容）
     KEYSTORE_PASSWORD: （你的密钥库密码）
     KEY_ALIAS: my-app
     KEY_PASSWORD: （你的密钥密码）
     ```

#### 步骤 C：修改 workflow

在 `.github/workflows/build-apk.yml` 中添加：

```yaml
- name: 🔐 配置签名
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > android/app/release-key.jks
    
- name: 📦 构建 Release APK
  env:
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: |
    cd android
    ./gradlew assembleRelease
```

并在 `android/app/build.gradle` 中配置：

```gradle
android {
    signingConfigs {
        release {
            storeFile file('release-key.jks')
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. 自动创建 Release

在 workflow 末尾添加：

```yaml
- name: 🎉 创建 GitHub Release
  uses: softprops/action-gh-release@v1
  if: startsWith(github.ref, 'refs/tags/')
  with:
    files: release/*.apk
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

然后打 tag 推送：
```bash
git tag v1.0.0
git push origin v1.0.0
```

会自动在 Releases 页面创建发布，并附带 APK。

---

## 🎨 徽章（可选）

在 README.md 中添加构建状态徽章：

```markdown
[![Build APK](https://github.com/你的用户名/app-organizer/workflows/构建%20Android%20APK/badge.svg)](https://github.com/你的用户名/app-organizer/actions)
```

显示效果：
- ✅ 绿色：构建成功
- ❌ 红色：构建失败
- 🟡 黄色：构建中

---

## 🐛 常见问题

### Q1: 推送后没有触发构建？

**检查**：
1. workflow 文件路径是否正确：`.github/workflows/build-apk.yml`
2. 推送的分支是否是 main 或 master
3. 访问 Actions 页面，查看是否有禁用提示

**解决**：
- 在 Actions 页面点击 "I understand my workflows, go ahead and enable them"

### Q2: 构建失败了？

**查看日志**：
1. 进入 Actions 页面
2. 点击失败的构建任务
3. 展开红色❌的步骤查看详细错误

**常见错误**：
- Gradle 下载超时 → 重新运行 workflow
- 依赖下载失败 → 检查网络，或添加国内镜像
- 语法错误 → 检查代码是否有误

### Q3: 找不到 Artifacts？

**原因**：构建失败或未完成

**解决**：
1. 确保构建任务显示绿色✅
2. 滚动到页面最底部
3. 如果没有，检查 workflow 配置是否正确

### Q4: APK 下载后无法安装？

**检查**：
1. 是否解压了 ZIP 文件
2. 手机是否允许安装未知来源应用
3. 是否开启了"纯净模式"（某些品牌）

### Q5: 能否自动安装到手机？

**不能直接安装**，但可以：
1. 配置自动发布到 Release 页面
2. 生成下载链接或二维码
3. 手机扫码下载安装

---

## 📊 构建时间对比

| 构建类型 | 首次时间 | 后续时间 | 缓存 |
|---------|---------|---------|------|
| 本地（Android Studio） | 5-10分钟 | 1-2分钟 | ✅ |
| GitHub Actions（首次） | 5-8分钟 | - | ❌ |
| GitHub Actions（后续） | - | 2-3分钟 | ✅ |

---

## 🎁 额外功能

### 1. 每日自动构建

在 workflow 中添加：
```yaml
on:
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨2点自动构建
```

### 2. 多版本构建

同时构建 Debug 和 Release：
```yaml
strategy:
  matrix:
    build-type: [Debug, Release]
steps:
  - run: ./gradlew assemble${{ matrix.build-type }}
```

### 3. 通知到微信/邮箱

使用第三方 Action：
```yaml
- name: 📬 发送通知
  uses: actions/notification-action@v1
  with:
    webhook: ${{ secrets.WEBHOOK_URL }}
```

---

## ✨ 完整流程总结

```
1. 修改代码
   ↓
2. git add . && git commit -m "更新功能"
   ↓
3. git push
   ↓
4. GitHub Actions 自动开始构建（2-3分钟）
   ↓
5. 访问 Actions 页面
   ↓
6. 下载 Artifacts 中的 APK
   ↓
7. 传到手机安装
   ↓
8. 完成！
```

---

## 🎊 现在就开始吧！

### 快速命令（复制粘贴执行）：

```bash
# 进入项目目录
cd c:/Users/EDY/CodeBuddy/20260126090212

# 初始化 Git
git init

# 添加所有文件
git add .

# 提交
git commit -m "🎉 Initial commit: Android 应用整理工具"

# 连接到你的 GitHub 仓库（记得替换用户名！）
git remote add origin https://github.com/你的用户名/app-organizer.git

# 推送
git branch -M main
git push -u origin main
```

**推送完成后**：
1. 访问 `https://github.com/你的用户名/app-organizer/actions`
2. 看到构建任务在运行
3. 等待 2-3 分钟
4. 下载 APK
5. 安装到手机
6. 🎉 完成！

---

**有任何问题随时问我！** 😊
