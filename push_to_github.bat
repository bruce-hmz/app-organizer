@echo off
chcp 65001 >nul
echo ========================================
echo   推送代码到 GitHub
echo ========================================
echo.

:: 检查是否配置了 Git
git --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Git，请先安装 Git
    echo 下载地址: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo 📝 请输入你的 GitHub 用户名:
set /p username=

echo.
echo 📝 请输入仓库名称 (默认: app-organizer):
set /p repo_name=
if "%repo_name%"=="" set repo_name=app-organizer

echo.
echo ========================================
echo 配置信息:
echo   GitHub 用户名: %username%
echo   仓库名称: %repo_name%
echo   仓库地址: https://github.com/%username%/%repo_name%
echo ========================================
echo.

echo ⚠️  请确保已在 GitHub 创建了仓库: %repo_name%
echo    如果还没创建，请访问: https://github.com/new
echo.
pause

echo.
echo [1/5] 初始化 Git 仓库...
if not exist ".git" (
    git init
    echo ✅ Git 仓库初始化完成
) else (
    echo ℹ️  Git 仓库已存在
)

echo.
echo [2/5] 添加所有文件...
git add .
if errorlevel 1 (
    echo [错误] 添加文件失败
    pause
    exit /b 1
)
echo ✅ 文件添加完成

echo.
echo [3/5] 提交到本地仓库...
git commit -m "🎉 Initial commit: Android 应用整理工具"
if errorlevel 1 (
    echo ℹ️  可能没有新的更改需要提交
)
echo ✅ 提交完成

echo.
echo [4/5] 配置远程仓库...
git remote remove origin >nul 2>&1
git remote add origin https://github.com/%username%/%repo_name%.git
if errorlevel 1 (
    echo [错误] 配置远程仓库失败
    pause
    exit /b 1
)
echo ✅ 远程仓库配置完成

echo.
echo [5/5] 推送到 GitHub...
git branch -M main
git push -u origin main
if errorlevel 1 (
    echo.
    echo [错误] 推送失败！可能的原因：
    echo   1. GitHub 仓库不存在
    echo   2. 没有配置 Git 凭证
    echo   3. 网络连接问题
    echo.
    echo 💡 解决方案：
    echo   1. 访问 https://github.com/%username%/%repo_name% 确认仓库存在
    echo   2. 配置 Git 凭证或使用 SSH
    echo   3. 检查网络连接
    pause
    exit /b 1
)

echo.
echo ========================================
echo         ✅ 推送成功！
echo ========================================
echo.
echo 🎉 代码已推送到 GitHub！
echo.
echo 📍 仓库地址:
echo    https://github.com/%username%/%repo_name%
echo.
echo 🤖 GitHub Actions 构建:
echo    https://github.com/%username%/%repo_name%/actions
echo.
echo 💡 下一步:
echo    1. 访问 Actions 页面查看构建进度
echo    2. 等待 2-3 分钟构建完成
echo    3. 下载 Artifacts 中的 APK
echo    4. 传到手机安装
echo.
echo ========================================

:: 询问是否打开浏览器
echo.
set /p open_browser=是否打开 Actions 页面? (y/n): 
if /i "%open_browser%"=="y" (
    start https://github.com/%username%/%repo_name%/actions
)

pause
