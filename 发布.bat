@echo off
chcp 65001 >nul
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

:: === [1/7] 同步 RELEASE_NOTES.md 到 assets ===
echo.
echo [1/7] 同步 RELEASE_NOTES.md 到 assets...
if not exist "app\src\main\assets\" mkdir "app\src\main\assets"
copy /y "RELEASE_NOTES.md" "app\src\main\assets\RELEASE_NOTES.md" >nul 2>&1
if errorlevel 1 (
    echo 复制失败：请确认根目录 E:\dev\tomato\RELEASE_NOTES.md 存在
    goto :err
)
echo 已同步。

:: === [2/7] 自动升级 versionCode / versionName ===
echo.
echo [2/7] 升级版本号...
set NEW_VN=%VER%
powershell -NoProfile -Command "$c=[IO.File]::ReadAllText('app\build.gradle');$q=[char]34;$m=[regex]::Match($c,'defaultConfig\s*\{[\s\S]*?versionCode\s+(\d+)');$oldVC=$m.Groups[1].Value;$newVC=[string]([int]$oldVC+1);$c=[regex]::Replace($c,'(defaultConfig\s*\{[\s\S]*?versionCode\s+)\d+','${1}'+$newVC,1);$p2='(defaultConfig\s*\{[\s\S]*?versionName\s+)'+$q+'[^'+$q+']*'+$q;$oldVN=[regex]::Match($c,'versionName\s+'+$q+'([^'+$q+'])'+$q).Groups[1].Value;$c=[regex]::Replace($c,$p2,'${1}'+$q+$env:NEW_VN+$q,1);[IO.File]::WriteAllText('app\build.gradle',$c);Write-Host('  versionCode: '+$oldVC+' -> '+$newVC);Write-Host('  versionName: '+$oldVN+' -> '+$env:NEW_VN)"
if errorlevel 1 (
    echo 版本号升级失败，请检查 app\build.gradle
    goto :err
)

:: === [3/7] git add + commit ===
echo.
echo [3/7] 提交改动...
git add .
git diff-index --quiet HEAD --
if errorlevel 1 (
    git commit -m "release: v%VER%"
    echo 已提交。
) else (
    echo 无改动，跳过提交。
)

:: === [4/7] git pull ===
echo.
echo [4/7] 拉取远端最新...
git pull --rebase
if errorlevel 1 goto :err

:: === [5/7] git push ===
echo.
echo [5/7] 推送到 GitHub...
git push
if errorlevel 1 goto :err

:: === [6/7] 打标签 ===
echo.
echo [6/7] 打标签 v%VER%...
git tag v%VER%
if errorlevel 1 (
    echo 打标签失败（v%VER% 可能已存在），请换版本号
    goto :err
)

:: === [7/7] 推送标签 ===
echo.
echo [7/7] 推送标签，触发出包...
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