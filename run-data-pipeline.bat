@echo off
rem Runs the NUVA data pipeline stage by stage, in order:
rem   origin -> starterdata -> pipeline -> owl-transform -> reasoner -> integration -> export
rem A stage is skipped when its module is not listed in pom.xml, or when --skip <stage> is given.
rem Stops at the first failing stage and exits with that stage's exit code.
rem On a fresh clone this script FAILS at the origin stage (source files missing). See README.md.
rem
rem Usage: run-data-pipeline.bat [--skip <stage>]... [--dry-run] [--local-origin] [--deploy] [--input-directory <dir>]
rem Environment: ALT_DEPLOYMENT_REPOSITORY = <id>::<url>, needed by --deploy (default is the placeholder REPLACE_ME)

setlocal EnableExtensions
set "STAGES=origin starterdata pipeline owl-transform reasoner integration export"
if not defined ALT_DEPLOYMENT_REPOSITORY set "ALT_DEPLOYMENT_REPOSITORY=REPLACE_ME"
set "SKIPPED= "
set "DRY_RUN=0"
set "LOCAL_ORIGIN=0"
set "DEPLOY=0"
set "INPUT_DIR_ARG="

:parse
if "%~1"=="" goto parsed
if /I "%~1"=="--skip" (
  if "%~2"=="" (echo --skip needs a stage name 1>&2 & exit /b 2)
  set "SKIPPED=%SKIPPED%%~2 "
  shift & shift & goto parse
)
if /I "%~1"=="--dry-run" (set "DRY_RUN=1" & shift & goto parse)
if /I "%~1"=="--local-origin" (set "LOCAL_ORIGIN=1" & shift & goto parse)
if /I "%~1"=="--deploy" (set "DEPLOY=1" & shift & goto parse)
if /I "%~1"=="--input-directory" (
  if "%~2"=="" (echo --input-directory needs a directory 1>&2 & exit /b 2)
  set "INPUT_DIR_ARG=-Dinput.directory=%~2"
  shift & shift & goto parse
)
echo Unknown argument: %~1 1>&2
exit /b 2

:parsed
if "%DEPLOY%"=="1" if "%ALT_DEPLOYMENT_REPOSITORY%"=="REPLACE_ME" (
  echo --deploy needs ALT_DEPLOYMENT_REPOSITORY=^<id^>::^<url^>; it is still the placeholder REPLACE_ME. 1>&2
  exit /b 2
)
cd /d "%~dp0"

for %%S in (%STAGES%) do (
  call :stage %%S
  if errorlevel 1 (
    echo Stage '%%S' failed. Stopping. 1>&2
    exit /b 1
  )
)
echo === All requested stages finished ===
exit /b 0

:stage
set "STAGE=%~1"
echo %SKIPPED% | findstr /C:" %STAGE% " >nul && (echo === Stage: %STAGE% ^(skipped by --skip^) === & exit /b 0)
findstr /R /C:"^ *<module>nuva-%STAGE%</module>" pom.xml >nul || (echo === Stage: %STAGE% ^(skipped: module nuva-%STAGE% not in pom.xml^) === & exit /b 0)
echo === Stage: %STAGE% ===
set "PROFILES="
set "GOAL=install"
set "EXTRA="
if "%STAGE%"=="origin" (
  if "%LOCAL_ORIGIN%"=="1" set "PROFILES=generateDataLocal"
  if "%DEPLOY%"=="1" (set "GOAL=deploy" & set "EXTRA=-DaltDeploymentRepository=%ALT_DEPLOYMENT_REPOSITORY%")
)
if "%STAGE%"=="export" (
  if "%DEPLOY%"=="1" (set "GOAL=deploy" & set "PROFILES=deployExport" & set "EXTRA=-DaltDeploymentRepository=%ALT_DEPLOYMENT_REPOSITORY%")
)
set "PARG="
if defined PROFILES set "PARG=-P%PROFILES%"
set "CMD=mvnw.cmd --projects nuva-%STAGE% --also-make clean %GOAL% %PARG% -Dmaven.build.cache.enabled=false %INPUT_DIR_ARG% %EXTRA%"
echo + %CMD%
if "%DRY_RUN%"=="1" exit /b 0
call %CMD%
exit /b %ERRORLEVEL%
