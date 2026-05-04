@echo off
chcp 65001 > nul
title 苍穹外卖系统 - 停止服务

echo ==========================================
echo    苍穹外卖系统 (Sky Take Out) - 停止服务
echo ==========================================
echo.

echo [1/3] 正在停止 Java 后端服务...
taskkill /F /IM java.exe 2>nul
echo          Java 已停止
echo.

echo [2/3] 正在停止 Nginx 前端代理...
cd /d "%~dp0..\core\nginx"
nginx.exe -s stop 2>nul
taskkill /F /IM nginx.exe 2>nul
echo          Nginx 已停止
echo.

echo [3/3] 正在停止微信开发者工具...
taskkill /F /IM wechatdevtools.exe 2>nul
echo          微信开发者工具已停止
echo.

echo ==========================================
echo    所有服务已停止，端口已释放
echo ==========================================
echo.

REM 验证端口释放
echo 端口状态检查：
netstat -ano | findstr "8080.*LISTENING" >nul && echo   8080: 仍被占用 || echo   8080: 已释放
netstat -ano | findstr "8081.*LISTENING" >nul && echo   8081: 仍被占用 || echo   8081: 已释放

echo.
pause
