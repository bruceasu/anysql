@if "%DEBUG%" == "" @echo off
TITLE mock-http-server

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set APP_HOME=%~dp0..
set VERSION=1.0.0
set APP_NAME=ansql
set JAR=%APP_NAME%-%VERSION%.jar
set MAIN_CLASS=com.asql.tools.ASQL
set APP_ID=127.0.0.1:1
set APP_CONF_DIR=${APP_DIR}/conf
set APP_ARGS=%1 %2 %3 %4 %5 %6 %7 %8 %9

@rem Add default JVM options here. You can also use JAVA_OPTS
@rem to pass JVM options to this script.
set JAVA_OPTS=%JAVA_OPTS% -Dapp.home=%APP_HOME% -Dapp.id=%APP_ID% -Djava.awt.headless=true
::: -Xms1g -Xmx1g -XX:PermSize=64M  -XX:+UseConcMarkSweepGC



set CLASSPATH=%APP_HOME%\conf;%APP_HOME%\*;%APP_HOME%\lib\*

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line



@rem Execute Gradle
"%JAVA_EXE%" %JAVA_OPTS% -classpath "%CLASSPATH%" %MAIN_CLASS% %APP_ARGS% %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1


:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
