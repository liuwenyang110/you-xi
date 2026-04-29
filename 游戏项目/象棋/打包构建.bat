@echo off
chcp 65001 >nul
title 中国象棋 - 构建中...
echo.
echo   正在构建生产版本...
echo.
cd /d "%~dp0"
call npm run build
echo.
echo   ╔══════════════════════════════╗
echo   ║     构建完成！               ║
echo   ║  输出目录: dist\             ║
echo   ╚══════════════════════════════╝
echo.
echo   双击打开 dist\index.html 即可游玩
echo.
pause
