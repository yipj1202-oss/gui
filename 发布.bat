@echo off
title 晷 · 一键发布

cd /d E:\dev\tomato
if errorlevel 1 goto :err

echo.
echo ========================================
echo   晷 · 一键发布
echo ========================================
echo.

set /p VER=请输入版本号（例如 1.1）: 
if "%VER%"=="" goto :err

echo.
echo [1/5] 暂存并检查本地改动...
git add .
git diff-index --quiet HEAD --
if errorlevel 1 (
    git commit -m "release: v%VER%"
    echo 已提交本地改动。
) else (
    echo 无改动，跳过提交。
)

echo.
echo [2/5] 拉取远端最新代码...
git pull --rebase
if errorlevel 1 goto :err

echo.
echo [3/5] 推送到 GitHub...
git push
if errorlevel 1 goto :err

echo.
echo [4/5] 打标签 v%VER%...
git tag v%VER%
if errorlevel 1 goto :err

echo.
echo [5/5] 推送标签，触发自动出包...
git push --tags
if errorlevel 1 goto :err

echo.
echo ========================================
echo   发布成功！
echo.
echo   稍等 3 分钟，去这里下载 APK：
echo   https://github.com/yipj1202-oss/gui/releases
echo ========================================
goto :done

:err
echo.
echo === 出错了，请检查上方信息 ===
:done
pause