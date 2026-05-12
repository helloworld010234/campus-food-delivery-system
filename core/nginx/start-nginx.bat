@echo off
setlocal
set "NGINX_HOME=D:\sky-delivery\core\nginx"
set "NGINX_PREFIX=D:/sky-delivery/core/nginx/"
set "NGINX_EXE=%NGINX_HOME%\nginx.exe"

if not exist "%NGINX_EXE%" (
  echo [ERROR] nginx.exe not found: %NGINX_EXE%
  exit /b 1
)

echo [1/4] Testing config...
"%NGINX_EXE%" -t -p "%NGINX_PREFIX%" -c conf/nginx.conf
if errorlevel 1 (
  echo [ERROR] Config test failed.
  exit /b 1
)

echo [2/4] Starting nginx...
start "" /b "%NGINX_EXE%" -p "%NGINX_PREFIX%" -c conf/nginx.conf

timeout /t 1 >nul

echo [3/4] Checking listen port 8081...
netstat -ano | findstr "LISTENING" | findstr ":8081"
if errorlevel 1 (
  echo [WARN] Port 8081 not detected yet.
)

echo [4/4] HTTP health check...
powershell -NoProfile -Command "$u='http://localhost:8081/?v=' + [DateTimeOffset]::Now.ToUnixTimeMilliseconds(); try { $r=Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 8; Write-Host ('HTTP_STATUS=' + $r.StatusCode); exit 0 } catch { Write-Host ('HTTP_ERR=' + $_.Exception.Message); exit 1 }"
if errorlevel 1 (
  echo [ERROR] Nginx started but HTTP check failed.
  exit /b 1
)

echo [OK] Nginx started successfully.
exit /b 0
