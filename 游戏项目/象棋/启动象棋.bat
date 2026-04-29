@echo off
chcp 65001 >nul
title 中国象棋 - 启动中...
echo.
echo   ╔══════════════════════════════╗
echo   ║       中 国 象 棋            ║
echo   ║    Chinese Chess Game        ║
echo   ╚══════════════════════════════╝
echo.
echo   正在启动开发服务器...
echo   启动后请访问浏览器中显示的地址
echo   按 Ctrl+C 停止服务器
echo.
cd /d "%~dp0"
npm run dev
pause
