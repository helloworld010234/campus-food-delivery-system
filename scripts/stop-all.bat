@echo off
chcp 65001 > nul
title Sky Take Out - Stop All
setlocal EnableExtensions

echo ==========================================
echo    Sky Take Out - stop services
echo ==========================================
echo.

REM ---------------------------------------------------------------
REM  Stop Nginx gracefully first (it owns port 8081 + worker children)
REM ---------------------------------------------------------------
echo [1/4] Stopping Nginx ...
pushd "%~dp0..\core\nginx" >nul 2>&1
if exist nginx.exe (
    nginx.exe -s quit 2>nul
    REM Give it a moment to finish in-flight requests, then force-kill leftovers.
    timeout /t 2 /nobreak >nul
    taskkill /F /IM nginx.exe 2>nul >nul
    echo          Nginx stopped.
) else (
    echo          nginx.exe not found in core\nginx; skipping.
)
popd >nul 2>&1
echo.

REM ---------------------------------------------------------------
REM  Stop Java backend (mvn spring-boot:run forks a JVM)
REM
REM  We narrow taskkill to the listener on port 8080 first so we do
REM  not blow away unrelated java.exe processes (IntelliJ, etc.).
REM  As a fallback, kill any java.exe spawned from the project tree.
REM ---------------------------------------------------------------
echo [2/4] Stopping Java backend (port 8080) ...
set "_pid="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":8080 .*LISTENING"') do set "_pid=%%P"
if defined _pid (
    echo          Killing PID !_pid! on port 8080
    taskkill /F /PID %_pid% 2>nul >nul
) else (
    echo          No listener on port 8080.
)
REM Fallback: kill leftover Maven wrapper / child JVM if still around.
taskkill /F /IM mvn.cmd 2>nul >nul
echo.

REM ---------------------------------------------------------------
REM  Optional: WeChat devtools (kept for parity with prior script)
REM ---------------------------------------------------------------
echo [3/4] Stopping WeChat devtools (if running) ...
taskkill /F /IM wechatdevtools.exe 2>nul >nul
echo.

REM ---------------------------------------------------------------
REM  Verify ports are free
REM ---------------------------------------------------------------
echo [4/4] Port status:
netstat -ano | findstr /R /C:":8080 .*LISTENING" >nul && echo          8080: still bound || echo          8080: free
netstat -ano | findstr /R /C:":8081 .*LISTENING" >nul && echo          8081: still bound || echo          8081: free
echo.

echo ==========================================
echo    Stop sequence complete.
echo ==========================================
endlocal
pause
