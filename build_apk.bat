@echo off
chcp 65001 >nul
echo ========================================
echo    应用整理工具 - 自动打包脚本
echo ========================================
echo.

:: 检查是否在正确的目录
if not exist "android" (
    echo [错误] 请在项目根目录运行此脚本！
    pause
    exit /b 1
)

:: 进入 android 目录
cd android

echo [1/4] 正在清理旧的构建...
call gradlew.bat clean
if errorlevel 1 (
    echo [错误] 清理失败，请检查 Gradle 配置
    pause
    exit /b 1
)

echo.
echo [2/4] 正在构建 APK（这可能需要几分钟）...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo [错误] 构建失败，请查看错误信息
    pause
    exit /b 1
)

echo.
echo [3/4] 正在查找生成的 APK...
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
    echo [错误] 找不到生成的 APK 文件
    pause
    exit /b 1
)

:: 创建输出目录
echo.
echo [4/4] 正在复制 APK 到项目根目录...
cd ..
if not exist "release" mkdir release

copy "android\%APK_PATH%" "release\应用整理工具.apk" >nul
if errorlevel 1 (
    echo [错误] 复制 APK 失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo         ✅ 打包成功！
echo ========================================
echo.
echo APK 文件位置：
echo   release\应用整理工具.apk
echo.
echo 文件大小：
for %%A in ("release\应用整理工具.apk") do echo   %%~zA 字节
echo.
echo 下一步：
echo   1. 将 APK 传到手机
echo   2. 点击安装即可
echo.
echo ========================================
pause
