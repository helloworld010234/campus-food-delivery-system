@echo off
setlocal
set "NGINX_HOME=D:\sky\sky\nginx"
set "NGINX_PREFIX=D:/sky/sky/nginx/"
set "NGINX_EXE=%NGINX_HOME%\nginx.exe"

if not exist "%NGINX_EXE%" (
  echo [ERROR] nginx.exe not found: %NGINX_EXE%
  exit /b 1
)

echo [1/3] Sending graceful quit...
"%NGINX_EXE%" -s quit -p "%NGINX_PREFIX%" -c conf/nginx.conf

timeout /t 1 >nul

echo [2/3] Force-kill remaining nginx from this path (if any)...
powershell -NoProfile -Command "$target='D:\sky\sky\nginx\nginx.exe'; Get-CimInstance Win32_Process -Filter \"Name='nginx.exe'\" | Where-Object { $_.ExecutablePath -eq $target } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"

echo [3/3] Verifying port 8081 released...
netstat -ano | findstr "LISTENING" | findstr ":8081"
if errorlevel 1 (
  echo [OK] Nginx stopped.
) else (
  echo [WARN] Port 8081 still listening. Please check other services.
)

exit /b 0