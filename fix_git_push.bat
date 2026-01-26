@echo off
chcp 65001 >nul
echo ========================================
echo   Git 推送问题修复工具
echo ========================================
echo.

echo 检测到错误: SSL_read Connection was reset
echo 这是网络连接问题，我们来尝试修复...
echo.

echo ========================================
echo 请选择解决方案:
echo ========================================
echo.
echo [1] 禁用 SSL 验证 (最快，推荐)
echo [2] 使用 GitHub 镜像加速
echo [3] 配置代理 (如果你有代理)
echo [4] 使用 SSH 方式推送
echo [5] 增加缓冲区大小
echo [6] 一键尝试所有方案
echo.
set /p choice=请输入数字 (1-6): 

if "%choice%"=="1" goto disable_ssl
if "%choice%"=="2" goto use_mirror
if "%choice%"=="3" goto config_proxy
if "%choice%"=="4" goto use_ssh
if "%choice%"=="5" goto increase_buffer
if "%choice%"=="6" goto try_all
goto invalid

:disable_ssl
echo.
echo [方案1] 禁用 SSL 验证...
git config --global http.sslVerify false
git config --global https.sslVerify false
echo ✅ SSL 验证已禁用
echo.
echo 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo ❌ 推送仍然失败，尝试其他方案
    pause
    goto menu
) else (
    goto success
)

:use_mirror
echo.
echo [方案2] 使用 GitHub 镜像...
echo.
echo 请输入你的 GitHub 用户名:
set /p username=
echo 请输入仓库名称 (默认: app-organizer):
set /p repo_name=
if "%repo_name%"=="" set repo_name=app-organizer

echo.
echo 尝试镜像站点:
echo 1. https://github.com.cnpmjs.org/%username%/%repo_name%.git
echo 2. https://hub.fastgit.xyz/%username%/%repo_name%.git
echo.

git remote remove origin >nul 2>&1
git remote add origin https://github.com.cnpmjs.org/%username%/%repo_name%.git
echo ✅ 已切换到镜像站点
echo.
echo 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo ❌ 镜像推送失败，尝试下一个方案
    git remote remove origin >nul 2>&1
    git remote add origin https://hub.fastgit.xyz/%username%/%repo_name%.git
    git push -u origin main
    if errorlevel 1 (
        echo ❌ 所有镜像都失败了
        pause
        goto menu
    )
)
goto success

:config_proxy
echo.
echo [方案3] 配置代理...
echo.
echo 如果你有代理服务器 (如 127.0.0.1:7890)
echo 请输入代理地址 (格式: http://127.0.0.1:7890):
set /p proxy=

if "%proxy%"=="" (
    echo ❌ 未输入代理地址
    pause
    goto menu
)

git config --global http.proxy "%proxy%"
git config --global https.proxy "%proxy%"
echo ✅ 代理已配置
echo.
echo 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo ❌ 推送失败
    echo 💡 提示: 可能代理地址不正确
    pause
    goto menu
) else (
    goto success
)

:use_ssh
echo.
echo [方案4] 使用 SSH 方式...
echo.
echo ⚠️  这需要你已配置好 SSH 密钥
echo 如果还没配置，请选择其他方案
echo.
pause

echo 请输入你的 GitHub 用户名:
set /p username=
echo 请输入仓库名称 (默认: app-organizer):
set /p repo_name=
if "%repo_name%"=="" set repo_name=app-organizer

git remote remove origin >nul 2>&1
git remote add origin git@github.com:%username%/%repo_name%.git
echo ✅ 已切换到 SSH 方式
echo.
echo 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo ❌ SSH 推送失败
    echo 💡 提示: 可能需要先配置 SSH 密钥
    pause
    goto menu
) else (
    goto success
)

:increase_buffer
echo.
echo [方案5] 增加缓冲区大小...
git config --global http.postBuffer 524288000
git config --global https.postBuffer 524288000
echo ✅ 缓冲区已增加到 500MB
echo.
echo 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo ❌ 推送失败
    pause
    goto menu
) else (
    goto success
)

:try_all
echo.
echo [方案6] 一键尝试所有方案...
echo.

echo 🔧 方案1: 禁用 SSL 验证
git config --global http.sslVerify false
git config --global https.sslVerify false
echo ✅ SSL 验证已禁用
timeout /t 1 >nul

echo.
echo 🔧 方案2: 增加缓冲区
git config --global http.postBuffer 524288000
echo ✅ 缓冲区已增加
timeout /t 1 >nul

echo.
echo 🔧 方案3: 设置超时时间
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
echo ✅ 超时设置已优化
timeout /t 1 >nul

echo.
echo 📡 现在重试推送...
git push -u origin main
if errorlevel 1 (
    echo.
    echo ========================================
    echo   所有自动方案都失败了
    echo ========================================
    echo.
    echo 💡 建议:
    echo   1. 检查网络连接
    echo   2. 尝试使用手机热点
    echo   3. 使用 VPN/代理
    echo   4. 稍后再试
    echo.
    echo 或者手动选择其他方案 (返回菜单)
    pause
    goto menu
) else (
    goto success
)

:invalid
echo ❌ 无效的选择
pause
goto menu

:menu
cls
echo ========================================
echo   Git 推送问题修复工具
echo ========================================
echo.
goto start

:success
echo.
echo ========================================
echo         ✅ 推送成功！
echo ========================================
echo.
echo 🎉 代码已成功推送到 GitHub！
echo.
echo 📍 下一步:
echo   1. 访问 GitHub Actions 查看构建
echo   2. 等待 2-3 分钟
echo   3. 下载 APK
echo.

echo 请输入你的 GitHub 用户名:
set /p username=
echo 请输入仓库名称 (默认: app-organizer):
set /p repo_name=
if "%repo_name%"=="" set repo_name=app-organizer

echo.
echo 🤖 GitHub Actions 地址:
echo    https://github.com/%username%/%repo_name%/actions
echo.

set /p open_browser=是否打开浏览器查看? (y/n): 
if /i "%open_browser%"=="y" (
    start https://github.com/%username%/%repo_name%/actions
)

echo.
echo ========================================
pause
exit /b 0
