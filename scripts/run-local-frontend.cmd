@echo off
cd /d "%~dp0..\frontend"
"C:\Program Files\nodejs\npm.cmd" run dev -- --host 127.0.0.1 > "%~dp0..\frontend-local.out.log" 2> "%~dp0..\frontend-local.err.log"
