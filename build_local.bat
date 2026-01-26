@echo off
chcp 65001 >nul
echo ========================================
echo 本地构建 APK 脚本
echo ========================================
echo.

cd android

echo [1/3] 清理旧构建...
call gradlew.bat clean
if errorlevel 1 (
    echo ❌ 清理失败
    pause
    exit /b 1
)

echo.
echo [2/3] 构建 Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)

echo.
echo [3/3] 复制 APK 到根目录...
cd ..
if not exist "build_output" mkdir build_output

copy /Y "android\app\build\outputs\apk\debug\app-debug.apk" "build_output\App-Organizer-debug.apk"

echo.
echo ========================================
echo ✅ 构建成功！
echo ========================================
echo.
echo APK 位置: build_output\App-Organizer-debug.apk
echo.
echo 下一步操作：
echo 1. 将 APK 传输到手机（USB/微信/云盘）
echo 2. 在手机上安装测试
echo 3. 测试无误后运行 push_to_github.bat 提交
echo.
pause
