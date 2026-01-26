@echo off
chcp 65001 >nul
echo ========================================
echo 快速测试流程
echo ========================================
echo.
echo 选择测试方式：
echo.
echo 1. 本地构建（手动传输到手机）
echo 2. 构建并通过 USB 安装到手机（需要 ADB）
echo 3. 只构建不安装
echo 4. 测试完成，提交到 GitHub
echo.
set /p choice="请选择 (1-4): "

if "%choice%"=="1" (
    call build_local.bat
) else if "%choice%"=="2" (
    call build_and_install.bat
) else if "%choice%"=="3" (
    cd android
    call gradlew.bat assembleDebug
    cd ..
    echo.
    echo APK 位置: android\app\build\outputs\apk\debug\app-debug.apk
    pause
) else if "%choice%"=="4" (
    echo.
    echo 准备提交到 GitHub...
    echo.
    set /p confirm="确认已测试无误？(y/n): "
    if /i "!confirm!"=="y" (
        git add -A
        set /p message="请输入提交信息: "
        git commit -m "!message!"
        git push
        echo.
        echo ✅ 已提交到 GitHub，CI/CD 将自动构建
    )
) else (
    echo 无效选择
    pause
)
