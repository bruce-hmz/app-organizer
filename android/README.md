# 📱 Android 应用整理工具

## ✅ 这是真正的 Android 原生应用！

### 核心功能
✅ **自动读取手机上所有已安装的应用**  
✅ **智能分类**：根据包名和应用名自动分类  
✅ **多分类支持**：一个应用可属于多个分类  
✅ **品牌UI适配**：5种手机品牌风格  
✅ **实时搜索**：快速找到应用  
✅ **一键启动**：点击直接打开应用  

---

## 🚀 如何安装到手机

### 方式一：使用 Android Studio（推荐）

1. **安装 Android Studio**
   - 下载地址：https://developer.android.com/studio
   - 安装后打开

2. **导入项目**
   ```
   File → Open → 选择 android 文件夹
   ```

3. **连接手机**
   - 手机开启开发者模式
   - 开启 USB 调试
   - 用数据线连接电脑

4. **运行应用**
   - 点击顶部工具栏的 ▶️ 运行按钮
   - 选择你的手机设备
   - 等待安装完成

### 方式二：生成 APK 安装包

1. **在 Android Studio 中**
   ```
   Build → Generate Signed Bundle / APK
   → 选择 APK
   → 创建新的密钥或使用现有密钥
   → 选择 release
   → Finish
   ```

2. **安装 APK**
   - 将生成的 APK 文件传到手机
   - 点击安装即可

---

## 📁 项目结构

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/apporganizer/
│   │   │   ├── MainActivity.kt          # 主Activity
│   │   │   ├── AppInfo.kt              # 数据模型
│   │   │   ├── AppClassifier.kt        # 自动分类器
│   │   │   └── AppAdapter.kt           # RecyclerView适配器
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml   # 主界面布局
│   │   │   │   ├── item_app_grid.xml   # 网格布局（小米等）
│   │   │   │   └── item_app_list.xml   # 列表布局（OPPO）
│   │   │   ├── values/
│   │   │   │   ├── strings.xml         # 文字资源
│   │   │   │   ├── colors.xml          # 颜色资源
│   │   │   │   └── themes.xml          # 主题样式
│   │   └── AndroidManifest.xml         # 清单文件（权限配置）
│   └── build.gradle                    # App级别构建配置
├── build.gradle                        # 项目级别构建配置
├── settings.gradle                     # 项目设置
└── gradle.properties                   # Gradle属性配置
```

---

## 🔧 核心实现说明

### 1. 读取手机应用
```kotlin
// MainActivity.kt - getInstalledApps()
val pm = packageManager
val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

for (appInfo in installedApps) {
    val appName = pm.getApplicationLabel(appInfo).toString()
    val icon = pm.getApplicationIcon(appInfo)
    val packageName = appInfo.packageName
    // ... 处理应用信息
}
```

### 2. 自动分类
```kotlin
// AppClassifier.kt
object AppClassifier {
    private val categoryRules = mapOf(
        AppCategory.SOCIAL to listOf("wechat", "qq", "weibo", ...),
        AppCategory.WORK to listOf("dingtalk", "wework", ...),
        // ... 更多分类规则
    )
    
    fun classify(packageName: String, appName: String): List<AppCategory> {
        // 根据包名和应用名匹配分类
    }
}
```

### 3. 品牌UI适配
```kotlin
// AppInfo.kt
enum class BrandStyle(
    val displayName: String, 
    val spanCount: Int,      // 网格列数
    val useListLayout: Boolean  // 是否使用列表布局
) {
    XIAOMI("小米", 3, false),     // 3列网格
    HUAWEI("华为", 2, false),     // 2列大卡片
    OPPO("OPPO", 1, true),        // 列表布局
    VIVO("vivo", 4, false),       // 4列紧凑网格
    IPHONE("iPhone", 3, false)    // 3列圆角网格
}
```

### 4. 启动应用
```kotlin
// MainActivity.kt - launchApp()
private fun launchApp(appInfo: AppInfo) {
    val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
    if (intent != null) {
        startActivity(intent)  // 直接启动应用
    }
}
```

---

## 🎨 品牌风格展示

| 品牌 | 布局方式 | 列数 | 特点 |
|------|---------|------|------|
| 小米 | 网格 | 3列 | 清晰简洁，标准网格 |
| 华为 | 网格 | 2列 | 大卡片，商务风格 |
| OPPO | 列表 | 1列 | 横向列表，信息丰富 |
| vivo | 网格 | 4列 | 紧凑设计，高效利用空间 |
| iPhone | 网格 | 3列 | 圆角设计，优雅简约 |

---

## 📊 分类规则

应用会根据包名和应用名自动归类：

- **社交**：微信、QQ、微博、抖音、小红书等
- **办公**：钉钉、企业微信、WPS、飞书等
- **娱乐**：爱奇艺、腾讯视频、网易云音乐、QQ音乐等
- **工具**：高德地图、百度网盘、相机、备忘录等
- **购物**：淘宝、京东、拼多多、美团等
- **金融**：支付宝、各大银行App等
- **游戏**：王者荣耀、和平精英等游戏类应用
- **系统**：系统核心应用（会自动隐藏）
- **其他**：无法匹配的应用

---

## 🔐 权限说明

### AndroidManifest.xml 中的权限
```xml
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

**作用**：允许应用查询手机上所有已安装的应用列表

**隐私说明**：
- 仅在本地读取，不会上传任何数据
- 不访问应用内容，只读取应用名称和图标
- 符合 Android 11+ 的隐私规范

---

## 💡 使用说明

### 基本操作
1. **启动应用** → 自动加载手机上所有应用
2. **切换品牌** → 顶部选择器切换UI风格
3. **选择分类** → 点击标签筛选不同类别
4. **搜索应用** → 输入应用名快速查找
5. **打开应用** → 点击应用卡片直接启动

### 功能亮点
✅ **自动分类**：无需手动分类，智能识别  
✅ **实时更新**：安装新应用后重启自动识别  
✅ **多分类支持**：一个应用可显示多个标签  
✅ **快速搜索**：支持应用名和包名搜索  
✅ **品牌适配**：不同品牌不同体验  

---

## 🎯 技术特点

### Android 原生开发
- **语言**：Kotlin
- **UI框架**：Material Design 3
- **架构**：MVVM 模式
- **异步处理**：Kotlin Coroutines
- **列表优化**：RecyclerView + DiffUtil

### 性能优化
- ✅ 后台线程加载应用列表
- ✅ DiffUtil 高效更新列表
- ✅ ViewBinding 减少findViewById开销
- ✅ 图标缓存（系统自动处理）

---

## 🚧 已知限制

1. **Android 11+**：需要在应用商店上架才能完整获取所有应用（开发测试无影响）
2. **系统应用**：部分核心系统应用会自动过滤
3. **分类准确度**：基于规则匹配，可能有个别应用分类不准

---

## 🔄 扩展建议

### 功能增强
- [ ] 支持自定义分类规则
- [ ] 添加应用使用频率统计
- [ ] 支持应用拖拽排序
- [ ] 添加收藏夹功能
- [ ] 支持应用卸载

### UI优化
- [ ] 添加深色模式
- [ ] 更多品牌风格
- [ ] 自定义主题颜色
- [ ] 动画效果优化

---

## ✨ 成果总结

### 交付内容
✅ **完整的 Android 原生应用源码**  
✅ **可直接编译运行**  
✅ **真实读取手机应用**  
✅ **自动分类功能**  
✅ **5种品牌UI风格**  
✅ **即点即用，直接启动应用**  

### 这不是Demo，是成品！
- ✅ 真实调用系统API
- ✅ 完整的业务逻辑
- ✅ 优雅的代码结构
- ✅ 可直接安装使用

---

## 📱 现在就安装到你的手机上试试吧！

**步骤回顾**：
1. 用 Android Studio 打开 `android` 文件夹
2. 连接手机（开启USB调试）
3. 点击运行按钮 ▶️
4. 等待安装完成
5. 在手机上打开"应用整理工具"
6. 看到你手机上所有应用被自动分类！

🎉 **这才是真正能在手机上运行的应用整理工具！**
