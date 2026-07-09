@echo off
cd /d "%~dp0..\backend"
"C:\Program Files\Common Files\Oracle\Java\javapath\java.exe" -jar target\carton-erp-1.0.0.jar > "%~dp0..\backend-local.out.log" 2> "%~dp0..\backend-local.err.log"
