@echo off
chcp 65001 >nul
echo ========================================
echo 本地构建并安装到手机
echo ========================================
echo.

echo 检查手机连接...
adb devices
if errorlevel 1 (
    echo.
    echo ❌ ADB 未安装或不在 PATH 中
    echo 请先安装 Android SDK Platform Tools
    echo 下载地址: https://developer.android.com/studio/releases/platform-tools
    pause
    exit /b 1
)

echo.
echo 已连接的设备:
adb devices -l
echo.

set /p continue="确认手机已连接并继续？(y/n): "
if /i not "%continue%"=="y" (
    echo 取消操作
    pause
    exit /b 0
)

cd android

echo.
echo [1/4] 清理旧构建...
call gradlew.bat clean

echo.
echo [2/4] 构建 Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)

echo.
echo [3/4] 卸载旧版本...
adb uninstall com.apporganizer
echo （如果提示 Failure 是正常的，说明之前没安装过）

echo.
echo [4/4] 安装到手机...
adb install app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo ❌ 安装失败
    echo 提示：如果提示权限错误，请在手机上允许 USB 调试
    pause
    exit /b 1
)

cd ..

echo.
echo ========================================
echo ✅ 构建并安装成功！
echo ========================================
echo.
echo 应用已安装到手机，可以直接打开测试
echo 测试无误后运行 push_to_github.bat 提交代码
echo.
pause
