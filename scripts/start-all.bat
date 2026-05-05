@echo off
chcp 65001 > nul
title Sky Take Out - Start All
setlocal EnableExtensions EnableDelayedExpansion

echo ==========================================
echo    Sky Take Out - one-click start
echo ==========================================
echo.

REM ---------------------------------------------------------------
REM  Move to repo root (parent of this script's directory)
REM ---------------------------------------------------------------
set "BASEDIR=%~dp0.."
pushd "%BASEDIR%" >nul
set "REPO_ROOT=%CD%"

REM ---------------------------------------------------------------
REM  Make sure logs directory exists (kept on D drive, never C)
REM ---------------------------------------------------------------
if not exist "%REPO_ROOT%\logs" mkdir "%REPO_ROOT%\logs" >nul 2>&1

REM ---------------------------------------------------------------
REM  [1/5] Load .env if present.
REM
REM  We read line-by-line with `type` (not by executing the file)
REM  so secrets cannot be accidentally executed as shell commands.
REM  Lines beginning with # or ; are treated as comments.
REM ---------------------------------------------------------------
if exist "%REPO_ROOT%\.env" (
    echo [1/5] Loading environment variables from .env ...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%REPO_ROOT%\.env") do (
        set "_k=%%A"
        set "_v=%%B"
        REM Trim leading spaces/tabs
        for /f "tokens=* delims= 	" %%X in ("!_k!") do set "_k=%%X"
        if not "!_k!"=="" if not "!_k:~0,1!"==";" (
            set "!_k!=!_v!"
        )
    )
) else (
    echo [WARN] No .env found at %REPO_ROOT%\.env
    echo        Copy tooling\.env.example to .env and fill placeholders.
    echo.
)

REM ---------------------------------------------------------------
REM  Apply safe defaults so first-time runs do not crash.
REM  These are DEV defaults and MUST NOT be used in production.
REM ---------------------------------------------------------------
if not defined DB_USERNAME set "DB_USERNAME=root"
if not defined DB_PASSWORD set "DB_PASSWORD=root"
if not defined DB_HOST set "DB_HOST=localhost"
if not defined DB_PORT set "DB_PORT=3306"
if not defined DB_DATABASE set "DB_DATABASE=sky_take_out"
if not defined REDIS_HOST set "REDIS_HOST=localhost"
if not defined REDIS_PORT set "REDIS_PORT=6379"
if not defined REDIS_PASSWORD set "REDIS_PASSWORD="
if not defined REDIS_DATABASE set "REDIS_DATABASE=0"
if not defined JWT_ADMIN_SECRET_KEY set "JWT_ADMIN_SECRET_KEY=dev_only_admin_secret_must_be_at_least_64_bytes_long_xxxxxxxxxxxxxxx"
if not defined JWT_USER_SECRET_KEY set "JWT_USER_SECRET_KEY=dev_only_user_secret_must_be_at_least_64_bytes_long_xxxxxxxxxxxxxxxx"
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev"

REM ---------------------------------------------------------------
REM  [2/5] Length sanity check on JWT secrets (warn only)
REM ---------------------------------------------------------------
call :strlen JWT_ADMIN_SECRET_KEY _admin_len
call :strlen JWT_USER_SECRET_KEY _user_len
if !_admin_len! LSS 64 (
    echo [WARN] JWT_ADMIN_SECRET_KEY is only !_admin_len! chars; HS512 needs ^>=64.
)
if !_user_len! LSS 64 (
    echo [WARN] JWT_USER_SECRET_KEY is only !_user_len! chars; HS512 needs ^>=64.
)

echo [2/5] Environment summary
echo          DB     : %DB_USERNAME%@%DB_HOST%:%DB_PORT%/%DB_DATABASE%
echo          Redis  : %REDIS_HOST%:%REDIS_PORT% (db %REDIS_DATABASE%)
echo          Profile: %SPRING_PROFILES_ACTIVE%
echo.

REM ---------------------------------------------------------------
REM  [3/5] Pre-check port 8080 and 8081
REM ---------------------------------------------------------------
echo [3/5] Checking ports 8080 and 8081 ...
netstat -ano | findstr /R /C:":8080 .*LISTENING" >nul && (
    echo [WARN] Port 8080 is already in use. Backend may fail to start.
    echo        Run scripts\stop-all.bat first or free the port manually.
)
netstat -ano | findstr /R /C:":8081 .*LISTENING" >nul && (
    echo [WARN] Port 8081 is already in use. Nginx may fail to start.
)
echo.

REM ---------------------------------------------------------------
REM  [4/5] Start Java backend (port 8080) in a new window
REM ---------------------------------------------------------------
echo [4/5] Starting Java backend on port 8080 ...
echo        First compile can take 1-2 minutes.
start "Sky Backend" cmd /c "cd /d %REPO_ROOT%\core\backend\sky-server && mvn spring-boot:run -DskipTests"

REM Wait for backend compile (rough delay; replaced by health probe below)
timeout /t 25 /nobreak > nul

REM Best-effort health probe against /internal/health (max ~30s)
set "_ready=0"
for /l %%i in (1,1,15) do (
    if "!_ready!"=="0" (
        powershell -NoProfile -Command "try { (Invoke-WebRequest -Uri 'http://localhost:8080/internal/health' -UseBasicParsing -TimeoutSec 2).StatusCode } catch { 0 }" 2>nul | findstr "200" >nul && set "_ready=1"
        if "!_ready!"=="0" timeout /t 2 /nobreak > nul
    )
)
if "!_ready!"=="1" (
    echo        Backend is responding on /internal/health.
) else (
    echo        Backend did not answer /internal/health yet; continuing anyway.
)

REM ---------------------------------------------------------------
REM  [5/5] Start Nginx on port 8081
REM ---------------------------------------------------------------
echo.
echo [5/5] Starting Nginx on port 8081 ...
pushd "%REPO_ROOT%\core\nginx" >nul
start "" nginx.exe
popd >nul
timeout /t 3 /nobreak > nul

echo.
echo ==========================================
echo    All services launched.
echo ==========================================
echo.
echo URLs:
echo   Admin dashboard : http://localhost:8081/#/login
echo   Swagger / API   : http://localhost:8080/doc.html
echo   Health check    : http://localhost:8080/internal/health
echo.
echo Default accounts:
echo   Admin     : admin / 123456
echo   Mini-app  : mock_code  (when MOCK_USER_LOGIN=true)
echo.
echo Logs:
echo   Backend window  : look at the "Sky Backend" cmd window
echo   Nginx access/err: %REPO_ROOT%\core\nginx\logs\
echo.
echo Press any key to open the admin dashboard ...
pause > nul
start "" http://localhost:8081/#/login

echo.
echo [Tip] Run scripts\stop-all.bat to stop everything.
echo.
popd >nul
endlocal
pause
goto :eof

REM ---------------------------------------------------------------
REM  :strlen <varName> <outVar>  -- length of an env var (cmd hack)
REM ---------------------------------------------------------------
:strlen
setlocal EnableDelayedExpansion
set "_s=!%~1!"
set "_n=0"
:strlen_loop
if defined _s (
    set "_s=!_s:~1!"
    set /a _n+=1
    goto strlen_loop
)
endlocal & set "%~2=%_n%"
goto :eof
