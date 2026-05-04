@echo off
chcp 65001 > nul
title 苍穹外卖系统 - 一键启动

echo ==========================================
echo    苍穹外卖系统 (Sky Take Out) - 一键启动
echo ==========================================
echo.

REM 设置工作目录为脚本所在目录的父目录
set "BASEDIR=%~dp0.."
cd /d "%BASEDIR%"

REM 读取 .env 文件中的环境变量
if exist .env (
    echo [1/4] 正在加载环境变量...
    for /f "usebackq tokens=1,2 delims==" %%a in (`.env`) do (
        set "%%a=%%b"
    )
) else (
    echo [警告] 未找到 .env 文件，将使用默认值。
    echo          请复制 tooling\.env.example 到 .env 并填写实际值。
    echo.
)

REM 设置最小必要环境变量（若 .env 未提供）
if not defined DB_USERNAME set DB_USERNAME=root
if not defined DB_PASSWORD set DB_PASSWORD=root
if not defined DB_HOST set DB_HOST=localhost
if not defined DB_PORT set DB_PORT=3306
if not defined DB_DATABASE set DB_DATABASE=sky_take_out
if not defined REDIS_HOST set REDIS_HOST=localhost
if not defined REDIS_PORT set REDIS_PORT=6379
if not defined REDIS_PASSWORD set REDIS_PASSWORD=
if not defined REDIS_DATABASE set REDIS_DATABASE=0
if not defined JWT_ADMIN_SECRET_KEY set JWT_ADMIN_SECRET_KEY=this_is_a_very_long_admin_secret_key_for_dev_only
if not defined JWT_USER_SECRET_KEY set JWT_USER_SECRET_KEY=this_is_a_very_long_user_secret_key_for_dev_only
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=dev

echo [2/4] 环境变量检查完成
echo          DB: %DB_USERNAME%@%DB_HOST%:%DB_PORT%/%DB_DATABASE%
echo          Redis: %REDIS_HOST%:%REDIS_PORT%
echo.

REM 启动后端服务
echo [3/4] 正在启动 Java 后端服务 (端口 8080)...
start "苍穹外卖 - Java 后端" cmd /c "cd /d %BASEDIR%\core\backend\sky-server && mvn spring-boot:run -DskipTests"

echo          后端启动中，首次编译可能需要 1-2 分钟...
echo.

REM 等待后端编译（简单延迟）
timeout /t 25 /nobreak > nul

REM 启动 Nginx
echo [4/4] 正在启动 Nginx 前端代理 (端口 8081)...
cd /d "%BASEDIR%\core\nginx"
start nginx.exe

echo          Nginx 已启动
echo.

REM 等待 Nginx 启动
timeout /t 3 /nobreak > nul

echo ==========================================
echo    所有服务已启动！
echo ==========================================
echo.
echo 访问地址：
echo   - 管理后台登录页 : http://localhost:8081/#/login
echo   - Swagger API 文档: http://localhost:8080/doc.html
echo   - 后端健康检查    : http://localhost:8080/user/shop/status
echo.
echo 默认账号：
echo   - 管理员 : admin / 123456
echo   - 小程序 : mock_code (模拟微信登录)
echo.
echo 按任意键打开浏览器访问管理后台...
pause > nul

start http://localhost:8081/#/login

echo.
echo [提示] 如需停止服务，请运行 scripts\stop-all.bat
echo.
pause
